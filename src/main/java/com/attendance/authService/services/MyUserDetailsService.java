package com.attendance.authService.services;

import com.attendance.authService.entity.User;
import com.attendance.authService.enums.ErrorCodeEnum;
import com.attendance.authService.exceptions.UserNotFoundException;
import com.attendance.authService.network.RolePermissionClient;
import com.attendance.authService.repo.UserRepo;
import com.attendance.authService.util.MyUserDetails;
import com.attendance.authService.network.RoleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RolePermissionClient rolePermissionClient;

    @Autowired
    private  RoleClient roleClient;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=userRepo.findByEmail(username)
                .orElseThrow(()->new UserNotFoundException( ErrorCodeEnum.S_404.getMessage()));

        return new MyUserDetails(user,roleClient,rolePermissionClient);
    }
}
