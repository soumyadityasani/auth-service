package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class SignUpResponseDto {

    private UUID id;

}
