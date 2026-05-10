package com.attendance.authService.repo;

import com.attendance.authService.entity.PendingOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PendingOtpRepo extends JpaRepository<PendingOtp,String> {
    boolean existsByContact(String contact);

    void deleteById(String contact);


    @Modifying
    @Query("DELETE FROM PendingOtp p WHERE p.expiry < :expiryTheshold")
    void deleteByExpiryBefore(LocalDateTime expiryTheshold);
}
