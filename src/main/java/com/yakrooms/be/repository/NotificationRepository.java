package com.yakrooms.be.repository;

import com.yakrooms.be.model.entity.Notification;
import com.yakrooms.be.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Batch delete operations
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.room.hotel.id = :hotelId")
    void deleteByHotelIdInBatch(@Param("hotelId") Long hotelId);
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user LEFT JOIN FETCH n.room WHERE n.user = :user")
    List<Notification> findByUserWithAssociations(@Param("user") User user);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user LEFT JOIN FETCH n.room WHERE n.user.id = :userId AND n.isRead = false")
    List<Notification> findByUserAndIsReadFalseWithAssociations(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.user LEFT JOIN FETCH n.room WHERE n.user.id = :userId")
    List<Notification> findByUserIdWithAssociations(@Param("userId") Long userId);
    
    // Find notification by booking and type
    @Query("SELECT n FROM Notification n WHERE n.booking = :booking AND n.type = :type")
    Optional<Notification> findByBookingAndType(@Param("booking") com.yakrooms.be.model.entity.Booking booking, @Param("type") String type);
} 