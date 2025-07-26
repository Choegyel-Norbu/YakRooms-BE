package com.yakrooms.be.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StaffRequestDTO {

	private String email;
	private Long hotelId;
	private String position;
	private String phoneNumber;
	private LocalDate dateJoined;

	public String getPosition() {
		return position;
	}

	public LocalDate getDateJoined() {
		return dateJoined;
	}

	public void setDateJoined(LocalDate dateJoined) {
		this.dateJoined = dateJoined;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}