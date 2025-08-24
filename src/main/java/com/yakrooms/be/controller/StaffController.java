package com.yakrooms.be.controller;

import com.yakrooms.be.dto.StaffRequestDTO;
import com.yakrooms.be.dto.StaffResponseDTO;
import com.yakrooms.be.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    @Autowired
    private StaffService staffService;

    // Add new staff member - Only HOTEL_ADMIN can add
    @PreAuthorize("hasRole('HOTEL_ADMIN')")
    @PostMapping
    public ResponseEntity<StaffResponseDTO> addStaff(@Validated @RequestBody StaffRequestDTO requestDTO) {
        StaffResponseDTO response = staffService.addStaff(requestDTO);
        return ResponseEntity.ok(response);
    }

    // Get staff by hotel ID with pagination - HOTEL_ADMIN and STAFF can access
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/hotel/{hotelId}/page")
    public ResponseEntity<Page<StaffResponseDTO>> getStaffByHotelIdPaginated(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<StaffResponseDTO> staffPage = staffService.getStaffByHotelId(hotelId, pageable);
        return ResponseEntity.ok(staffPage);
    }

    // Get staff by hotel ID - HOTEL_ADMIN and STAFF can access
    @PreAuthorize("hasAnyRole('HOTEL_ADMIN', 'STAFF')")
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<StaffResponseDTO>> getStaffByHotelId(@PathVariable Long hotelId) {
        return ResponseEntity.ok(staffService.getStaffByHotelId(hotelId));
    }

    // Delete staff member - Only HOTEL_ADMIN can delete
    @PreAuthorize("hasRole('HOTEL_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaffById(id);
        return ResponseEntity.noContent().build();
    }
}
