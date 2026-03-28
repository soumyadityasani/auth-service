package com.attendance.authService.repo;

import com.attendance.authService.dto.StudentCountDto;
import com.attendance.authService.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepo extends JpaRepository<Student, UUID>{

    @Query("""
SELECT new com.attendance.authService.dto.StudentCountDto(
    s.department,
    s.academicYear,
    s.semester,
    COUNT(s)
)
FROM Student s
GROUP BY s.department, s.academicYear, s.semester
""")
    List<StudentCountDto> getStudentCountsGrouped();

    Long countByDepartmentAndAcademicYearAndSemester(
            String department,
            String academicYear,
            String semester
    );

    // 🔹 Find student by studentId (unique)
    Optional<Student> findByStudentId(String studentId);

    @Query("""
       SELECT DISTINCT s.academicYear 
       FROM Student s 
       WHERE s.department = :department 
       AND s.semester = :semester
       """)
    List<String> findAcademicYears(String department, String semester);
}
