package com.yakrooms.be.dto.mapper;

import com.yakrooms.be.dto.NotificationDTO;
import com.yakrooms.be.model.entity.Notification;

public class NotificationMapper {
    public static NotificationDTO toDto(Notification n) {
        return new NotificationDTO(
            n.getId(),
            n.getUser() != null ? n.getUser().getName() : null,
            n.getRoom() != null ? n.getRoom().getRoomNumber() : null,
            n.getTitle(),
            n.getMessage(),
            n.getType(),
            n.isRead(),
            n.getCreatedAt()
        );
    }
} 