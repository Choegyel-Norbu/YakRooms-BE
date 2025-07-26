package com.yakrooms.be.service;

import com.yakrooms.be.dto.StaffRequestDTO;
import com.yakrooms.be.dto.StaffResponseDTO;
import java.util.List;

public interface StaffService {
    StaffResponseDTO addStaff(StaffRequestDTO requestDTO);
    List<StaffResponseDTO> getStaffByHotelId(Long hotelId);
    void deleteStaffById(Long id);
} 