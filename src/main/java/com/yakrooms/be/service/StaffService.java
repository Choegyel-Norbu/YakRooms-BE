package com.yakrooms.be.service;

import com.yakrooms.be.dto.StaffRequestDTO;
import com.yakrooms.be.dto.StaffResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {
    StaffResponseDTO addStaff(StaffRequestDTO requestDTO);
    
    // Optimized method with pagination
    Page<StaffResponseDTO> getStaffByHotelId(Long hotelId, Pageable pageable);
    
    // Legacy method for backward compatibility
    List<StaffResponseDTO> getStaffByHotelId(Long hotelId);
    
    void deleteStaffById(Long id);
} 