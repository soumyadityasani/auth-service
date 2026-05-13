package com.attendance.authService.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRoleWhiteListDto {
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private List<String> assignedRole;
}
