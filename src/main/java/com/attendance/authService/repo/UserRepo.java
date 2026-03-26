package com.attendance.authService.repo;

import com.attendance.authService.entity.Student;
import com.attendance.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
     Optional<User> findByEmail(String email);

     Optional<User> findByUsername(String username);

     Optional<User> findByStudent(Student student);

    boolean existsByEmail(String email);

//    @Modifying
//    @Transactional
//    @Query("DELETE FROM User u WHERE u.role= :roleId")
//    Optional<Integer> deleteAllUserByRoleId(@Param("roleId") Long id);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id IN :userIds")
    int deleteAllByIdIn(@Param("userIds") List<UUID> userIds);

}
