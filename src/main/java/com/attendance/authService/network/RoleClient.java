package com.attendance.authService.network;

import com.attendance.authService.dto.ApiResonseDto;
import com.attendance.authService.dto.RoleResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "role-service",url = "${roleClient-service-url}")
public interface RoleClient {

    @GetMapping("/get-role-by-name")
    ApiResonseDto<RoleResponseDto> getRoleByName(@RequestParam String role);

    @GetMapping("/get-role-by-id/{id}")
    ApiResonseDto<RoleResponseDto> getRoleById(@PathVariable Long id);
}
