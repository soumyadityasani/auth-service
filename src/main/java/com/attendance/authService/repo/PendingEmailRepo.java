package com.attendance.authService.repo;

import com.attendance.authService.entity.PendingEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingEmailRepo extends JpaRepository<PendingEmail,String> {
    Optional<PendingEmail> findByEmailVerificationToken(String token);


    void deleteById(String id);
}
