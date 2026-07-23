package com.attendance.authService.entity;

import com.attendance.authService.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// DeviceBindingAudit.java
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_binding_audit")
public class DeviceBindingAudit {
    @Id
    @GeneratedValue
    private UUID id;

    // No cascade relation — nullable FK, no JPA cascade at all
    @Column(name = "user_id")
    private String userId;

    @Column(name = "old_device_id")
    private String oldDeviceId;

    @Column(name = "new_device_id")
    private String newDeviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;   // enum: BIND, UNBIND, REBIND

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(name = "performed_by")
    private String performedBy;
}
