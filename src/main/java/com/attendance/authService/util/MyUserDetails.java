package com.attendance.authService.util;

import com.attendance.authService.dto.ApiResponseDto;
import com.attendance.authService.dto.PermissionResponseDto;
import com.attendance.authService.dto.RoleResponseDto;
import com.attendance.authService.entity.User;
import com.attendance.authService.network.RoleClient;
import com.attendance.authService.network.RolePermissionClient;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MyUserDetails implements UserDetails {

    private User user;
    private final RolePermissionClient rolePermissionClient;
    private final RoleClient roleClient;

    @Getter
    private String role;

    private List<String> permission;

    public MyUserDetails(User user, RoleClient roleClient,RolePermissionClient rolePermissionClient){
        this.user=user;
        this.roleClient=roleClient;
        this.rolePermissionClient=rolePermissionClient;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){

        if (role == null) {
            ApiResponseDto<RoleResponseDto> responseDto = roleClient.getRoleById(user.getRole());
            role = responseDto.getData().getRole();
        }

        if(permission==null){
            ApiResponseDto<List<PermissionResponseDto>> response =
                    rolePermissionClient.getPermissionForRole(role);

            permission = response.getData()
                    .stream()
                    .map(PermissionResponseDto::getPermission)
                    .toList();

            for(String perm:permission){
                System.out.println(perm);
            }
        }

//        return Collections.singleton(new SimpleGrantedAuthority(role));

        return permission.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    //Custom
    public String getFullName() {
        return user.getUsername();
    }

    public String getCollegeRoll(){
        return user.getCollegeRoll();
    }

    public String getContact(){
        return user.getContact();
    }

    public Long getRoleLong(){
        return user.getRole();
    }

    public String getStudentId(){
        return user.getStudentId();
    }

    public String getDepartment(){
        return user.getDepartment();
    }

    public String getAdmissionYear(){
        return user.getAdmission_year();
    }



}
