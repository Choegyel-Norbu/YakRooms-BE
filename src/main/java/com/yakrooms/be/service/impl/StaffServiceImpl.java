package com.yakrooms.be.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Staff;
import com.yakrooms.be.repository.StaffRepository;

@Service
public class StaffServiceImpl {
	@Autowired
	private StaffRepository staffRepository;

	public List<Staff> getAllStaff() {
		return staffRepository.findAll();
	}

	public Staff getStaffById(Long id) throws ResourceNotFoundException {
		return staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
	}

	public Staff createStaff(Staff staff) {
		return staffRepository.save(staff);
	}

	public Staff updateStaff(Long id, Staff details) throws ResourceNotFoundException {
		Staff staff = getStaffById(id);
		// update fields
		staff.setFirstName(details.getFirstName());
		// ... other setters
		return staffRepository.save(staff);
	}

	public void deleteStaff(Long id) throws ResourceNotFoundException {
		staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
		staffRepository.deleteById(id);
	}
}
