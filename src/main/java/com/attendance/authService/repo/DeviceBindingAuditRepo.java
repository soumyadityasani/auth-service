package com.attendance.authService.repo;

import com.attendance.authService.entity.DeviceBindingAudit;
import com.attendance.authService.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceBindingAuditRepo extends JpaRepository<DeviceBindingAudit, UUID> {
    Optional<DeviceBindingAudit> findTopByUserIdAndNewDeviceIdIsNullOrderByPerformedAtDesc(String userId);

    Optional<DeviceBindingAudit> findTopByUserIdOrderByPerformedAtDesc(String userId);

    Optional<DeviceBindingAudit> findTopByUserIdAndActionAndNewDeviceIdIsNullOrderByPerformedAtDesc(String userId, AuditAction auditAction);
}
