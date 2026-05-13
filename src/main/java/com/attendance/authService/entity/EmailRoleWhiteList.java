package com.attendance.authService.entity;

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
    private String email;

    private List<String> assignedRole;   // ✅ admin sets this: "ROLE_FACULTY", "ROLE_HOD" etc

    private boolean used;          // prevent re-registration
}
