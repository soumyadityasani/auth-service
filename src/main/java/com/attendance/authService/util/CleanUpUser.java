package com.attendance.authService.util;

import com.attendance.authService.repo.PendingOtpRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@EnableScheduling
public class CleanUpUser {

    @Autowired
    private PendingOtpRepo pendingOtpRepo;

    @Scheduled (fixedRate = 24 * 60 * 60 * 1000 )
    @Transactional
    public void cleanUpExpiredPendingOtp(){
        LocalDateTime expiryThreshold=LocalDateTime.now().minusMinutes(15);
        pendingOtpRepo.deleteByExpiryBefore(expiryThreshold);
    }
}
