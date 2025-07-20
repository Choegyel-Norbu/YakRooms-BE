package com.yakrooms.be.dto.request;

import java.util.List;

import com.yakrooms.be.model.entity.RoomItem;
import com.yakrooms.be.model.enums.RoomType;

public class RoomRequest {
	public String roomType;
	public String description;
	public Double price;
	public boolean available;
	public int maxGuests;
	public List<String> amenities;
	public String roomNumber;
	public List<String> imageUrl;

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

}
