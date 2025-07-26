package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    
    @Query("SELECT s FROM Staff s JOIN FETCH s.user u JOIN FETCH s.hotel h")
    List<Staff> findAllWithUserAndHotel();
    
    @Query("SELECT s FROM Staff s JOIN FETCH s.user u WHERE s.hotel.id = :hotelId")
    List<Staff> findAllByHotelIdWithUser(@Param("hotelId") Long hotelId);
}

