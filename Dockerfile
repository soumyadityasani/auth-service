# ============================================================
# STAGE 1: DEPENDENCY CACHE
# Goal: Cache Maven dependencies separately so they aren't
#        re-downloaded on every source-code change.
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS deps

# Set a clean working directory inside the container
WORKDIR /app

# Copy ONLY the POM file first.
# Docker builds in layers. By copying pom.xml alone and running
# dependency:go-offline, this layer is cached until pom.xml changes.
# If only src/ changes, this expensive step is skipped entirely.
COPY pom.xml .

# Download all dependencies into the local Maven cache (~/.m2).
# -B = batch/non-interactive mode (no progress bars, cleaner logs).
# This layer is cached until pom.xml changes.
RUN mvn -B dependency:go-offline


# ============================================================
# STAGE 2: BUILD
# Goal: Compile source code and package the fat JAR.
#        Runs only when source code actually changes.
# ============================================================
FROM deps AS build

# Copy application source code.
# This is done AFTER dependency caching so a code-only change
# doesn't bust the dependency layer above.
COPY src ./src

# Compile and package the application.
# -DskipTests        → skip unit tests (run them in CI separately).
# -Dspring-boot.repackage.excludeDevtools=true → shave KB off the jar.
# -q                 → quiet output; errors still surface.
RUN mvn -B -q -DskipTests \
    -Dspring-boot.repackage.excludeDevtools=true \
    clean package


# ============================================================
# STAGE 3: EXTRACT (Spring Boot layered JARs)
# Goal: Use Spring Boot's layertools to split the fat JAR into
#       distinct Docker layers (dependencies, app classes, etc.)
#       so only changed layers are re-pushed on each deployment.
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS extract

WORKDIR /app

# Copy the assembled fat JAR from the build stage.
# Only this stage and later stages contain any Maven/JDK tooling.
COPY --from=build /app/target/*.jar app.jar

# Explode the JAR into layered directories using Spring Boot's
# built-in layertools. This produces:
#   dependencies/   → third-party libs (rarely changes)
#   spring-boot-loader/ → Spring loader classes (rarely changes)
#   snapshot-dependencies/ → SNAPSHOT libs (changes sometimes)
#   application/    → your compiled classes (changes every build)
RUN java -Djarmode=layertools -jar app.jar extract


# ============================================================
# STAGE 4: RUNTIME (final image)
# Goal: The leanest possible production image.
#       Contains ONLY the JRE + your app — no Maven, no JDK,
#       no build tools, no source code, no test classes.
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Run as a non-root user for security.
# -S = system account (no home dir, no shell, no password).
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy each layer from the extract stage in dependency order
# (least-changed → most-changed) so Docker can cache them
# individually. Only the 'application' layer changes most builds.
COPY --from=extract /app/dependencies/          ./
COPY --from=extract /app/spring-boot-loader/    ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/           ./

# Switch to the non-root user before the process starts.
USER spring

# Expose the default Spring Boot port. EXPOSE is documentation only
# — it does not publish the port. Use -p 8080:8080 at docker run.
EXPOSE 8080

# JVM tuning flags:
# -XX:+UseContainerSupport          → respect cgroup CPU/memory limits
# -XX:MaxRAMPercentage=75.0         → use up to 75% of container RAM
# -XX:+UseG1GC                      → G1 garbage collector (default, explicit)
# -Djava.security.egd=...           → faster startup by using /dev/urandom
# JarLauncher is Spring Boot's layered-jar entry point.
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]