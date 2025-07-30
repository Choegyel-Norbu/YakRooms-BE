package com.yakrooms.be.dto.mapper;

import com.yakrooms.be.dto.StaffRequestDTO;
import com.yakrooms.be.dto.StaffResponseDTO;
import com.yakrooms.be.model.entity.Staff;
import com.yakrooms.be.model.entity.User;

public class StaffMapper {
  
    public static StaffResponseDTO toDto(Staff staff, User user) {
        StaffResponseDTO dto = new StaffResponseDTO();
        dto.setStaffId(staff.getId());
        dto.setHotelId(staff.getHotel().getId());
        dto.setPosition(staff.getPosition());
        dto.setFullName(user.getName());
        dto.setStaffEmail(staff.getEmail());
        dto.setNumber(staff.getPhoneNumber());
        dto.setDateJoined(staff.getDateJoined());
        // For staff, we'll use the first role or combine all roles
        String rolesString = user.getRoles().stream()
                .map(role -> role.name())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        dto.setRole(rolesString);
        dto.setProfilePictureUrl(user.getProfilePicUrl());
        
        return dto;
    }
} 