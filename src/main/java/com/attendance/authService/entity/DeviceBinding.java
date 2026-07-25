package com.attendance.authService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_bindings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"device_hardware_id"})
        })
public class DeviceBinding {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "device_hardware_id", nullable = true)
    private String deviceHardwareId;

    @Column(name = "bound_at", nullable = false)
    private LocalDateTime boundAt = LocalDateTime.now();

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "unbind_requested", nullable = false)
    private Boolean unbindRequested = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unbind_approved_by")
    private User unbindApprovedBy;

    @Column(name = "unbind_approved_at")
    private LocalDateTime unbindApprovedAt;
}