package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Staff;
import com.yakrooms.be.projection.StaffProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    
    Optional<Staff> findByEmail(String email);
    
    // Optimized query using projection to avoid N+1 and LazyInitializationException
    @Query("SELECT " +
           "s.id as staffId, " +
           "s.hotel.id as hotelId, " +
           "s.email as staffEmail, " +
           "u.name as fullName, " +
           "s.position as position, " +
           "s.phoneNumber as phoneNumber, " +
           "GROUP_CONCAT(ur) as roles, " +
           "u.profilePicUrl as profilePictureUrl, " +
           "s.dateJoined as dateJoined " +
           "FROM Staff s " +
           "JOIN s.user u " +
           "LEFT JOIN u.roles ur " +
           "WHERE s.hotel.id = :hotelId " +
           "GROUP BY s.id, s.hotel.id, s.email, u.name, s.position, s.phoneNumber, u.profilePicUrl, s.dateJoined")
    Page<StaffProjection> findStaffProjectionsByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);
    
    // Alternative: Using @EntityGraph for better performance
    @Query("SELECT s FROM Staff s " +
           "JOIN FETCH s.user u " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE s.hotel.id = :hotelId")
    List<Staff> findAllByHotelIdWithUserAndRoles(@Param("hotelId") Long hotelId);
    
    // Legacy method for backward compatibility
    @Query("SELECT DISTINCT s FROM Staff s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.hotel h " +
           "WHERE s.hotel.id = :hotelId")
    List<Staff> findAllByHotelIdWithUser(@Param("hotelId") Long hotelId);
    
    // Count staff by hotel for pagination info
    @Query("SELECT COUNT(s) FROM Staff s WHERE s.hotel.id = :hotelId")
    long countByHotelId(@Param("hotelId") Long hotelId);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, Long id);
}