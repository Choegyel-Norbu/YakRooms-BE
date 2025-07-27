package com.yakrooms.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    Optional<User> findByHotelIdAndRole(Long hotelId, Role role);


}
