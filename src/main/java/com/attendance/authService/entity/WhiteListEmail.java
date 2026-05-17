package com.attendance.authService.entity;

import com.attendance.authService.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "whitelisted_emails")
@Data
@NoArgsConstructor
public class WhiteListEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    @Convert(converter = EncryptionConverter.class) // ✅ ADD THIS
    private String email;

    private boolean used = false; // true once student registers

    public WhiteListEmail(String email) {
        this.email = email.toLowerCase().trim();
    }
}
