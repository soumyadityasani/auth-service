package com.attendance.authService.services;

import com.attendance.authService.dto.ApiResponseDto;
import com.attendance.authService.dto.PermissionResponseDto;
import com.attendance.authService.dto.RoleResponseDto;
import com.attendance.authService.entity.User;
import com.attendance.authService.network.RoleClient;
import com.attendance.authService.network.RolePermissionClient;
import com.attendance.authService.repo.UserRepo;
import com.attendance.authService.repo.UserRoleRepo;
import com.attendance.authService.util.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserRoleRepo userRoleRepo;

    @Autowired
    private RolePermissionClient rolePermissionClient;

    @Autowired
    private  RoleClient roleClient;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Long> roleIds = userRoleRepo.findRoleIdsByUserId(user.getId());

        if (roleIds.isEmpty()) {
            throw new UsernameNotFoundException("User has no roles assigned");
        }

// Fetch roles
        ApiResponseDto<List<RoleResponseDto>> roleResponse = roleClient.getRolesByIds(roleIds);

        if (!roleResponse.isSuccess() || roleResponse.getData() == null || roleResponse.getData().isEmpty()) {
            throw new UsernameNotFoundException("Failed to fetch roles");
        }

        List<String> roles = roleResponse.getData()
                .stream()
                .map(RoleResponseDto::getRole)
                .toList();

        // ✅ 4. Fetch Permissions (Feign)
        ApiResponseDto<List<PermissionResponseDto>> permResponse;
        try {
            permResponse = roleClient.getPermissionsForRoles(roles);
        } catch (Exception e) {
            throw new RuntimeException("Permission service unavailable", e);
        }

        // ✅ Validate permission response
        if (permResponse == null ||
                !permResponse.isSuccess() ||
                permResponse.getData() == null ||
                permResponse.getData().isEmpty()) {

            throw new RuntimeException("No permissions found for roles");
        }

        List<String> permissions = permResponse.getData()
                .stream()
                .map(PermissionResponseDto::getPermission)
                .distinct()
                .toList();

        // ❗ Final safety check
        if (permissions.isEmpty()) {
            throw new RuntimeException("User has no permissions assigned");
        }

        // ✅ 5. Return UserDetails
        return new MyUserDetails(user, roles, permissions);
    }
}
