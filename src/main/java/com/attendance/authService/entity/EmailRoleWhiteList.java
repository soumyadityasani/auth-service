package com.attendance.authService.entity;

import com.attendance.authService.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "email_whitelist")
@Data
public class EmailRoleWhiteList {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    @Convert(converter = EncryptionConverter.class) // ✅ ADD THIS
    private String email;

    private List<String> assignedRole;   // ✅ admin sets this: "ROLE_FACULTY", "ROLE_HOD" etc

    private boolean used;          // prevent re-registration
}
