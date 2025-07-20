package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.yakrooms.be.model.enums.RoomType;

public class RoomResponse {
	public Long id;
	public RoomType roomType;
	public String description;
	public Double price;
	public boolean isAvailable;
	public int maxGuests;
	public int roomNumber;
	public LocalDateTime createdAt;
	public LocalDateTime updatedAt;
	private List<String> amenities;
}
