package com.attendance.authService.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetHodOrCoordinatorRequest {

    private String department;

    private String academicYear;

    private String semester;
}
