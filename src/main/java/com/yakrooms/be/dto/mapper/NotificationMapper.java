package com.yakrooms.be.dto.mapper;

import com.yakrooms.be.dto.NotificationDTO;
import com.yakrooms.be.model.entity.Notification;

public class NotificationMapper {
    public static NotificationDTO toDto(Notification n) {
        if (n == null) {
            return null;
        }
        
        String userName = null;
        String roomNumber = null;
        
        try {
            if (n.getUser() != null) {
                userName = n.getUser().getName();
            }
        } catch (Exception e) {
            // Handle LazyInitializationException gracefully
            userName = "Unknown User";
        }
        
        try {
            if (n.getRoom() != null) {
                roomNumber = n.getRoom().getRoomNumber();
            }
        } catch (Exception e) {
            // Handle LazyInitializationException gracefully
            roomNumber = "Unknown Room";
        }
        
        return new NotificationDTO(
            n.getId(),
            userName,
            roomNumber,
            n.getTitle(),
            n.getMessage(),
            n.getType(),
            n.isRead(),
            n.getCreatedAt()
        );
    }
} 