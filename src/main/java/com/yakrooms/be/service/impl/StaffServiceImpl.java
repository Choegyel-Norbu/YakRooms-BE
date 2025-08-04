package com.yakrooms.be.service.impl;

import com.yakrooms.be.dto.StaffRequestDTO;
import com.yakrooms.be.dto.StaffResponseDTO;
import com.yakrooms.be.dto.mapper.StaffMapper;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Staff;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.projection.StaffProjection;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.StaffRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {
    
    private static final Logger log = LoggerFactory.getLogger(StaffServiceImpl.class);
    
    private final StaffRepository staffRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public StaffServiceImpl(StaffRepository staffRepository,
                           HotelRepository hotelRepository,
                           UserRepository userRepository) {
        this.staffRepository = staffRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public StaffResponseDTO addStaff(StaffRequestDTO requestDTO) {
        log.debug("Adding new staff with email: {}", requestDTO.getEmail());
        
        if (staffRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        Hotel hotel = hotelRepository.findById(requestDTO.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));
        
        User user = new User();
        user.setEmail(requestDTO.getEmail());
        user.addRole(Role.STAFF);
        user.setActive(true);
        user.setHotel(hotel);
        user = userRepository.save(user);
        
        Staff staff = new Staff();
        staff.setEmail(requestDTO.getEmail());
        staff.setPhoneNumber(requestDTO.getPhoneNumber());
        staff.setHotel(hotel);
        staff.setUser(user);
        staff.setPosition(requestDTO.getPosition());
        staff.setDateJoined(requestDTO.getDateJoined());
        staff = staffRepository.save(staff);
        
        log.info("Successfully created staff with id: {}", staff.getId());
        return StaffMapper.toDto(staff, user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<StaffResponseDTO> getStaffByHotelId(Long hotelId, Pageable pageable) {
        log.debug("Fetching staff for hotel id: {} with pagination", hotelId);
        
        // Use optimized projection query to avoid N+1 and LazyInitializationException
        Page<StaffProjection> staffProjections = staffRepository.findStaffProjectionsByHotelId(hotelId, pageable);
        
        // Convert projections to DTOs
        Page<StaffResponseDTO> staffDtos = staffProjections.map(StaffMapper::toDto);
        
        log.info("Successfully fetched {} staff members for hotel id: {}", staffDtos.getTotalElements(), hotelId);
        return staffDtos;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StaffResponseDTO> getStaffByHotelId(Long hotelId) {
        log.debug("Fetching all staff for hotel id: {} (legacy method)", hotelId);
        
        // Use the optimized query with JOIN FETCH to avoid LazyInitializationException
        List<Staff> staffList = staffRepository.findAllByHotelIdWithUserAndRoles(hotelId);
        
        return staffList.stream()
            .map(staff -> StaffMapper.toDto(staff, staff.getUser()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteStaffById(Long id) {
        log.debug("Deleting staff with id: {}", id);
        
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", "id", id));
        
        if (staff.getUser() != null) {
            userRepository.delete(staff.getUser());
        }
        
        staffRepository.deleteById(id);
        log.info("Successfully deleted staff with id: {}", id);
    }
}