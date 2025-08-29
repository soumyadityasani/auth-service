package com.attendance.authService.network;

import com.attendance.authService.dto.ApiResponseDto;
import com.attendance.authService.dto.PermissionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "role-permission-service", url = "${rolePermissionClient-service-url}")
public interface RolePermissionClient {

    @GetMapping("/{role}/permission")
    public ApiResponseDto<List<PermissionResponseDto>> getPermissionForRole(@PathVariable String role);
}
