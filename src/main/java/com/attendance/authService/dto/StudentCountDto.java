package com.attendance.authService.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class StudentCountDto {

    private String department;
    private String academicYear;
    private String semester;
    private Long count;

    public StudentCountDto(String department, String academicYear, String semester, Long count) {
        this.department = department;
        this.academicYear = academicYear;
        this.semester = semester;
        this.count = count;
    }
}