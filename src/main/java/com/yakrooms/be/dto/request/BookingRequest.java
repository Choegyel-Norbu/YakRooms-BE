package com.yakrooms.be.dto.request;

import java.time.LocalDate;

public class BookingRequest {
	private Long userId;
	private Long hotelId;
	private Long roomId;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private int guests;
	private int numberOfbookingRooms;
	private Double totalPrice;

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public int getNumberOfbookingRooms() {
		return numberOfbookingRooms;
	}

	public void setNumberOfbookingRooms(int numberOfbookingRooms) {
		this.numberOfbookingRooms = numberOfbookingRooms;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public LocalDate getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
	}

	public LocalDate getCheckOutDate() {
		return checkOutDate;
	}

	public void setCheckOutDate(LocalDate checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public int getGuests() {
		return guests;
	}

	public void setGuests(int guests) {
		this.guests = guests;
	}

}
