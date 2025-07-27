package com.yakrooms.be.dto;

import java.time.LocalDate;
import com.yakrooms.be.model.enums.RoomType;

public class RoomStatusDTO {
	private String roomNumber;
	private RoomType roomType;
	private String status;
	private String currentGuest;
	private LocalDate checkoutDate;

	public RoomStatusDTO() {
	}

	public RoomStatusDTO(String roomNumber, RoomType roomType, String status, String currentGuest,
			LocalDate checkoutDate) {
		this.roomNumber = roomNumber;
		this.roomType = roomType;
		this.status = status;
		this.currentGuest = currentGuest;
		this.checkoutDate = checkoutDate;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public RoomType getRoomType() {
		return roomType;
	}

	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCurrentGuest() {
		return currentGuest;
	}

	public void setCurrentGuest(String currentGuest) {
		this.currentGuest = currentGuest;
	}

	public LocalDate getCheckoutDate() {
		return checkoutDate;
	}

	public void setCheckoutDate(LocalDate checkoutDate) {
		this.checkoutDate = checkoutDate;
	}
}