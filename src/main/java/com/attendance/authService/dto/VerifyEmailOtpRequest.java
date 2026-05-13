package com.attendance.authService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyEmailOtpRequest {
    @Email(message = "INVALID EMAIL FORMAT")
    @Pattern(
            regexp = "^[a-z0-9]+@(gmail\\.com|yahoo\\.com|outlook\\.com|hotmail\\.com|protonmail\\.com|icloud\\.com)$",
            message = "Only Gmail, Yahoo, Outlook, Hotmail, Protonmail or Icloud addresses are allowed"
    )
    @NotBlank(message = "EMAIL IS REQUIRED")
    @Size(max=50, message = "MAX  50 CHARACTERS")
    private String email;

    @NotBlank(message = "MAX 6 CHAR")
    private String code;
}
