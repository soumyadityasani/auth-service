package com.attendance.authService.repo;

import com.attendance.authService.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepo extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUser_Id(UUID userId);

    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.user.id = :userId")
    List<Long> findRoleIdsByUserId(UUID userId);


    @Query("SELECT ur.user.id FROM UserRole ur WHERE ur.roleId = :roleId")
    List<UUID> findUserIdsByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);

    boolean existsByRoleIdIn(List<Long> roleIds);

    long countByUser_Id(UUID userId);



}
