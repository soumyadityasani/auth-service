package com.attendance.authService.network;

import com.attendance.authService.dto.ApiResponseDto;
import com.attendance.authService.dto.PermissionResponseDto;
import com.attendance.authService.dto.RoleResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "role-permission",url = "${roleClient-service-url}",
configuration = RoleClientConfig.class)
public interface RoleClient {

    @GetMapping("/get-role-by-name")
    ApiResponseDto<RoleResponseDto> getRoleByName(@RequestParam String role);

    @GetMapping("/get-role-by-id/{id}")
    ApiResponseDto<RoleResponseDto> getRoleById(@PathVariable Long id);

    @GetMapping("/get-roles-by-ids")
    ApiResponseDto<List<RoleResponseDto>> getRolesByIds(@RequestParam List<Long> ids);

    @GetMapping("/get-roles-by-names")
    ApiResponseDto<List<RoleResponseDto>> getRolesByNames(@RequestParam List<String> roles);

    @GetMapping("/permissions")
    public ApiResponseDto<List<PermissionResponseDto>> getPermissionsForRoles(@RequestParam List<String> roles);
}
