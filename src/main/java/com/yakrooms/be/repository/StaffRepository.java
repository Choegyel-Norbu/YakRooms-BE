package com.yakrooms.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yakrooms.be.model.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> { }

