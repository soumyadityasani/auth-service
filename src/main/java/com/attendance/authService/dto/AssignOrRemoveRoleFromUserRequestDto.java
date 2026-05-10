package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignOrRemoveRoleFromUserRequestDto {

    private String username;

    private List<String> roles;
}
