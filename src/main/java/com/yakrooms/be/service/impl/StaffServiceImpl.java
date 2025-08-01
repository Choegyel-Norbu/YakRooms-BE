package com.yakrooms.be.service.impl;

import com.yakrooms.be.dto.StaffRequestDTO;
import com.yakrooms.be.dto.StaffResponseDTO;
import com.yakrooms.be.dto.mapper.StaffMapper;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Staff;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.StaffRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.StaffService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {
	@Autowired
	private StaffRepository staffRepository;

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	@Transactional
	public StaffResponseDTO addStaff(StaffRequestDTO requestDTO) {
		if (staffRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
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

		return StaffMapper.toDto(staff, user);
	}

	@Override
	public List<StaffResponseDTO> getStaffByHotelId(Long hotelId) {
		return staffRepository.findAllByHotelIdWithUser(hotelId).stream()
				.map(staff -> StaffMapper.toDto(staff, staff.getUser())).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void deleteStaffById(Long id) {
		Staff staff = staffRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Staff not found"));

		User user = staff.getUser();

		if (user != null) {
			// Delete user roles directly using native SQL to avoid foreign key constraint
			jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());
			
			// Delete bookings associated with this user to avoid room constraint
			jdbcTemplate.update("DELETE FROM booking WHERE user_id = ?", user.getId());
			
			// Break the bidirectional relationship
			user.setStaff(null);
			userRepository.save(user);
		}

		// Delete the staff first, then the user
		staffRepository.deleteById(id);
		
		if (user != null) {
			userRepository.deleteById(user.getId());
		}
	}
}
