package com.attendance.authService.repo;

import com.attendance.authService.entity.Hod;
import com.attendance.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HodRepo extends JpaRepository<Hod, UUID> {

    Optional<Hod> findByHodAndDepartmentAndAcademicYearAndSemester(User hod, String department, String academicYear, String semester);

    Optional<Hod> findByDepartmentAndAcademicYearAndSemesterAndActiveTrue(String department, String academicYear, String semester);

}
