package com.attendance.authService.repo;

import com.attendance.authService.entity.EmailRoleWhiteList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailRoleWhiteListRepo extends JpaRepository<EmailRoleWhiteList, UUID> {

    Optional<EmailRoleWhiteList> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

}
