package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;

public class RoomResponse {
	public Long id;
	public String roomType;
	public String description;
	public Double price;
	public boolean isAvailable;
	public int maxGuests;
	public LocalDateTime createdAt;
	public LocalDateTime updatedAt;
}
