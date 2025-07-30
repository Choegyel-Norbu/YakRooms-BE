package com.yakrooms.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.yakrooms.be.model.entity.UserRole;
import com.yakrooms.be.model.enums.Role;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdAndRole(Long userId, Role role);

    void deleteByUserIdAndRole(Long userId, Role role);

    boolean existsByUserIdAndRole(Long userId, Role role);
} 