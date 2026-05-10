package com.attendance.authService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {

    @NotBlank(message = "PASSWORD IS REQUIRE")
    @Size(max=12, message = "MAX 12 CHARACTERS")
    private String password;



    @NotBlank(message = "CONFIRM PASSWORD IS REQUIRE")
    @Size(max=12, message = "MAX 12 CHARACTERS")
    private String newPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
