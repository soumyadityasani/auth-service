package com.attendance.authService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingOtp {

    @Id
    private String contact; // use contact number as ID
    private String otp;
    private LocalDateTime expiry;
}
