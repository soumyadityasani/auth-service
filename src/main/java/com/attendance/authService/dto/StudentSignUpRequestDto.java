package com.attendance.authService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentSignUpRequestDto extends  BaseSignUpRequestDto {

    @NotBlank(message = "STUDENT ID IS REQUIRE")
    @Size(max=10, message = "MAX 10 CHARACTERS")
    private String studentId;

    @NotBlank(message = "COLLEGE ROLL IS REQUIRE")
    @Size(max=30, message = "MAX 30 CHARACTERS")
    private String collegeRoll;

//    @NotBlank(message = "DEPARTMENT IS REQUIRE")
//    @Size(max=20, message = "MAX 20 CHARACTERS")
//    private String department;

//    @NotBlank(message = "ACADEMIC YEAR IS REQUIRE")
//    @Size(max=30, message = "MAX 30 CHARACTERS")
//    private String academic_year;

    @NotBlank(message = "SEMESTER IS REQUIRE")
    @Size(max=1, message = "MAX 1 CHARACTERS")
    private String semester;

}
