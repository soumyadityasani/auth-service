package com.attendance.authService.config;

import com.attendance.authService.dto.ApiResponseDto;
import com.attendance.authService.dto.RoleResponseDto;
import com.attendance.authService.entity.User;
import com.attendance.authService.entity.UserRole;
import com.attendance.authService.network.RoleClient;
import com.attendance.authService.repo.UserRepo;
import com.attendance.authService.repo.UserRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class AccountsSeeder implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserRoleRepo userRoleRepo;

    @Autowired
    private RoleClient roleClient;

    @Override
    public void run(String... args) throws Exception {

        boolean isAdminPresent=false;

        ApiResponseDto<List<RoleResponseDto>> roleResponse = null;

        List<String> role= new ArrayList<>();
        role.add("ADMIN");

        try {

            // ✅ FEIGN CALL
            roleResponse= roleClient.getRolesByNames(role);

            //ROLE RESPONSE
            List<Long> rolesId= roleResponse.getData()
                    .stream()
                    .map(RoleResponseDto::getId)
                    .toList();

            System.out.println("RoleId: "+rolesId);

            isAdminPresent= userRoleRepo.existsByRoleIdIn(rolesId);

        }catch (Exception e){
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ PROBLEM IN ACCOUNT SEEDING @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

            return;
        }

        //CHECH
        if(!isAdminPresent){

            // SAVE THE USER
            User user = new User();

            user.setUserId("T00000000");
            user.setUsername("Admin");
//            user.setCollegeRoll("T00000000");
            user.setDepartment("ALL");
            user.setEmail("admin@gmail.com");
            user.setContact("9111111111");
            user.setPassword(passwordEncoder.encode("admin@14"));


            List<RoleResponseDto> roles= roleResponse.getData();

            if(roles==null || roles.isEmpty()){
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ PROBLEM IN ACCOUNT SEEDING | ROLE EMPTY @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                System.exit(0);
            }

            User savedUser = userRepo.save(user);  //Ensure UUID generated

            List<UserRole> userRoles= roles.stream()
                    .map(rol-> new UserRole(savedUser,rol.getId()))
                    .toList();

            userRoleRepo.saveAll(userRoles);

            System.out.println("DEFAULT ACCOUNT(S) SEDED SUCCESSFUL");

        }


    }
}
