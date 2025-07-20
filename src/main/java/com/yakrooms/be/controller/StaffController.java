package com.yakrooms.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakrooms.be.model.entity.Staff;
import com.yakrooms.be.service.impl.StaffServiceImpl;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {
	@Autowired
	private StaffServiceImpl staffService;

	@GetMapping
	public List<Staff> getAllStaff() {
		return staffService.getAllStaff();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Staff> getStaffById(@PathVariable Long id) {
		Staff s = staffService.getStaffById(id);
		return ResponseEntity.ok(s);
	}

	@PostMapping
	public Staff createStaff(@RequestBody Staff staff) {
		return staffService.createStaff(staff);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Staff> updateStaff(@PathVariable Long id, @RequestBody Staff details) {
		Staff updated = staffService.updateStaff(id, details);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
		staffService.deleteStaff(id);
		return ResponseEntity.noContent().build();
	}
}
