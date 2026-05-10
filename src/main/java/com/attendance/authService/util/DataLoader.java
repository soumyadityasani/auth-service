//package com.canteen.authService.util;
//
//import com.canteen.authService.entity.Role;
//import com.canteen.authService.enums.RoleEnum;
//import com.canteen.authService.repo.RoleRepo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class DataLoader  implements CommandLineRunner {
//
//    @Autowired
//    private RoleRepo roleRepo;
//
//    @Override
//    public void run(String... args) throws Exception {
//        for (RoleEnum roleEnum:RoleEnum.values()){
//            roleRepo.findByRole(roleEnum).orElseGet(()->{
//                Role role=new Role();
//                role.setRole(roleEnum);
//
//                return  roleRepo.save(role);
//            });
//        }
//    }
//}
