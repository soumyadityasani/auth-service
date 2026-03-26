package com.attendance.authService.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateStudentRequestDto {

    @Size(max=10, message = "MAX  10 CHARACTERS")
    private String oldStudentId;

    @Size(max=10, message = "MAX  10 CHARACTERS")
    private String studentId;

    @Size(max=30, message = "MAX  30 CHARACTERS")
    private String collegeRoll;

    @Size(max=20, message = "MAX  20 CHARACTERS")
    private String department;

}
