package com.attendance.authService.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private final SecretKey secretKey ;

    @Value("${JWT_EXPIRY_MS}")
    private long jwtExpiryMs;



//    public JWTService(){
//        try {
//            KeyGenerator keyGen= KeyGenerator.getInstance("HmacSHA256");
//            SecretKey sk= keyGen.generateKey();
//            secretKey=Base64.getEncoder().encodeToString(sk.getEncoded());
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
     public JWTService() {
    // Generate a secure key at startup (or load from configuration)
    this.secretKey = Keys.hmacShaKeyFor(
            "SANI_SUPER_SECRET_123456789123456789123456789" // Your existing key
                    .getBytes(StandardCharsets.UTF_8)
    );

    // Alternatively, generate a new secure random key:
    // this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
}

    public String generateToken(String username) {
        Map<String,Object> claims= new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
//        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        return secretKey;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims=extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build().parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username=extractUsername(token);
        return(username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

