package com.attendance.authService.repo;

import com.attendance.authService.entity.DeviceBinding;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceBindingRepo extends JpaRepository<DeviceBinding, UUID> {
    Optional<DeviceBinding> findByUser_UserId(String userId);
    Page<DeviceBinding> findByUnbindRequestedTrueAndUser_Student_DepartmentAndUser_Student_AcademicYear(
            String department, String academicYear, Pageable pageable);

    Page<DeviceBinding> findByUnbindRequestedTrueAndUser_DepartmentInAndUser_Student_AcademicYearIn(List<String> departments, List<String> academicYears, Pageable pageable);
}
