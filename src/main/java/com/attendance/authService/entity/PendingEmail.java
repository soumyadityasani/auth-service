package com.attendance.authService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingEmail {

    @Id
    private String email;

    private String pendingEmail; // <== New email awaiting verification

    private String emailVerificationToken;

    private LocalDateTime tokenExpiry;
}
