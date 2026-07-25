package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingDeviceChangeDto {
    private String userId;
    private String username;
    private String department;
    private String academicYear;
    private String semester;
    private String rollNo;
    private String currentDeviceHash;
    private LocalDateTime boundAt;
    private LocalDateTime lastSeenAt;
}
