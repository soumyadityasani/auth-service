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
public class ProfileResponseDto {

    private String userId;
    private String username;
    private String department;
    private String email;
    private String contact;
    private List<String> role;

    private StudentProfileDto studentProfile= null;


}