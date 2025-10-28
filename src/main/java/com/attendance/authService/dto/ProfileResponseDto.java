package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponseDto {

    private String studentId;
    private String username;
    private String collegeRoll;
    private String department;
    private String email;
    private String contact;
    private String admission_year;
    private String role;


}