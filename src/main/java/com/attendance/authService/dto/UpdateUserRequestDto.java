package com.attendance.authService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequestDto {

    @NotBlank(message = "NAME IS REQUIRE")
    @Size(max=20, message = "MAX 20 CHARACTERS")
    private String username;

    @Email(message = "INVALID EMAIL FORMAT")
    @Pattern(
            regexp = "^[a-z0-9]+@(gmail\\.com|yahoo\\.com|outlook\\.com|hotmail\\.com|protonmail\\.com|icloud\\.com)$",
            message = "Only Gmail, Yahoo, Outlook, Hotmail, Protonmail or Icloud addresses are allowed"
    )
    @NotBlank(message = "EMAIL IS REQUIRED")
    @Size(max=50, message = "MAX  50 CHARACTERS")
    private String email;

    @NotBlank(message = "PASSWORD IS REQUIRE")
    @Size(max=12, message = "MAX 12 CHARACTERS")
    private String contact;

}
