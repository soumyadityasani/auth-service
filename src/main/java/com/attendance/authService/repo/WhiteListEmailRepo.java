package com.attendance.authService.repo;

import com.attendance.authService.entity.WhiteListEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhiteListEmailRepo extends JpaRepository<WhiteListEmail, UUID> {

    Optional<WhiteListEmail> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
