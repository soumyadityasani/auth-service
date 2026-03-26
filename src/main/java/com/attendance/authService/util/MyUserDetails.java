package com.attendance.authService.util;

import com.attendance.authService.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MyUserDetails implements UserDetails {

    private User user;

    @Getter
    private List<String> roles;   // ✅ MULTIPLE ROLES

    @Getter
    private List<String> permissions; // ✅ ALL PERMISSIONS

    public MyUserDetails(User user, List<String> roles, List<String> permissions) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // ✅ Combine ROLE + PERMISSIONS
        List<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        authorities.addAll(roles.stream()
                .map(role -> new SimpleGrantedAuthority(role)) // or "ROLE_" + role
                .collect(Collectors.toList()));


        return authorities;
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

    // ✅ Custom methods

    public String getFullName() {
        return user.getUsername();
    }

//    public String getCollegeRoll() {
//        return user.getCollegeRoll();
//    }

    public String getContact() {
        return user.getContact();
    }

//    public String getStudentId() {
//        return user.getStudentId();
//    }

    public String getDepartment() {
        return user.getDepartment();
    }

//    public String getAdmissionYear() {
//        return user.getAdmission_year();
//    }
}
