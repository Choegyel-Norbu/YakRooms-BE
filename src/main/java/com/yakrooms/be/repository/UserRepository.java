package com.yakrooms.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId AND :role MEMBER OF u.roles")
    Optional<User> findByHotelIdAndRole(@Param("hotelId") Long hotelId, @Param("role") Role role);


}
