package com.attendance.authService.repo;

import com.attendance.authService.entity.DeviceBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceBindingRepo extends JpaRepository<DeviceBinding, UUID> {
    Optional<DeviceBinding> findByUser_UserId(String userId);
}
