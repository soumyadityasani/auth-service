package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class StudentProfileDto {

    private String studentId;

    private String collegeRoll;

    private String admissionYear;

    private String academicYear;

    private String semester;
}
