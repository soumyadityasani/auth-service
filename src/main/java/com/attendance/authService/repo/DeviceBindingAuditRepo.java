package com.attendance.authService.repo;

import com.attendance.authService.entity.DeviceBindingAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceBindingAuditRepo extends JpaRepository<DeviceBindingAudit, UUID> {
}
