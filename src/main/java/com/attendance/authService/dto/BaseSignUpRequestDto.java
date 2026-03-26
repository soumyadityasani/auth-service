package com.attendance.authService.dto;


import com.attendance.authService.validations.PasswordMatches;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches
public class BaseSignUpRequestDto {

//    @NotBlank(message = "USER ID IS REQUIRE")
//    @Size(max=10, message = "MAX 10 CHARACTERS")
//    private String studentId;

    @NotBlank(message = "NAME IS REQUIRE")
    @Size(max=25, message = "MAX 25 CHARACTERS")
    private String username;

//    @NotBlank(message = "COLLEGE ROLL IS REQUIRE")
//    @Size(max=30, message = "MAX 30 CHARACTERS")
//    private String collegeRoll;

    @NotBlank(message = "DEPARTMENT IS REQUIRE")
    @Size(max=20, message = "MAX 20 CHARACTERS")
    private String department;

    @Email(message = "INVALID EMAIL FORMAT")
    @Pattern(
            regexp = "^[a-z0-9]+@(gmail\\.com|yahoo\\.com|outlook\\.com|hotmail\\.com|protonmail\\.com|icloud\\.com)$",
            message = "Only Gmail, Yahoo, Outlook, Hotmail, Protonmail or Icloud addresses are allowed"
    )
    @NotBlank(message = "EMAIL IS REQUIRED")
    @Size(max=50, message = "MAX  50 CHARACTERS")
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String contact;

    @NotBlank(message = "PASSWORD IS REQUIRE")
    @Size(max=12, message = "MAX 12 CHARACTERS")
    private String password;

    @NotBlank(message = "CONFIRM PASSWORD IS REQUIRE")
    @Size(max=12, message = "MAX 12 CHARACTERS")
    private String confirmPassword;

//    @Size(max=10, message = "MAX 10 CHARACTERS")
//    private String academic_year;

//    @Size(max=1, message = "MAX 1 CHARACTERS")
//    private String semester;

    @NotNull(message = "ROLE IS REQUIRE")
    private List<String> role;

}

