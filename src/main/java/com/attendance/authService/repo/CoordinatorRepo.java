package com.attendance.authService.repo;

import com.attendance.authService.entity.Coordinator;
import com.attendance.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoordinatorRepo extends JpaRepository<Coordinator, UUID> {
    Optional<Coordinator> findByCoordinatorAndDepartmentAndAcademicYearAndSemester(User coordinator, String department, String academicYear, String semester);

    Optional<Coordinator> findByDepartmentAndAcademicYearAndSemesterAndActiveTrue(String department, String academicYear, String semester);
}
