package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponseDto {
    private String username;
    private String email;
    private String studentId;
    private String collegeRoll;
    private String department;
    private String semester;
    private String academicYear;
}
