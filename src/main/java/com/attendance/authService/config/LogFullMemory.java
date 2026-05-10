package com.attendance.authService.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

//@Component
public class LogFullMemory {
    private static final Logger log = LoggerFactory.getLogger(LogFullMemory.class);

    @EventListener(ApplicationReadyEvent.class)
    public void logFullMemory() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long mb = 1024 * 1024;

        long heapUsed = memBean.getHeapMemoryUsage().getUsed() / mb;
        long nonHeapUsed = memBean.getNonHeapMemoryUsage().getUsed() / mb;
        long totalUsed = heapUsed + nonHeapUsed;

        log.info("--- FULL STARTUP MEMORY ---");
        log.info("Heap Used:    {} MB", heapUsed);
        log.info("Non-Heap:     {} MB", nonHeapUsed);
        log.info("Total Actual: {} MB", totalUsed);
        log.info("---------------------------");
    }
}
