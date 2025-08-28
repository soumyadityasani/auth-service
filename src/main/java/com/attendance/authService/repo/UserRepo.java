package com.attendance.authService.repo;

import com.attendance.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
     Optional<User> findByEmail(String email);

     Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.role= :roleId")
    Optional<Integer> deleteAllUserByRoleId(@Param("roleId") Long id);

}
