package com.attendance.authService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHodOrCoordinatorRequest {

    private String userId;

    private String newUserId;

    private String department;

    private String newDepartment;

    private String academicYear;

    private String newAcademicYear;

    private String semester;

    private String newSemester;

    private boolean newIsActive;
}
