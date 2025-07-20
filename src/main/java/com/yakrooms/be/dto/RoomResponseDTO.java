package com.yakrooms.be.dto;

import java.util.List;

import com.yakrooms.be.model.enums.RoomType;

public class RoomResponseDTO {

	private Long id;
	private RoomType roomType;
	private String description;
	private Double price;
	private boolean isAvailable;
	private int maxGuests;
	private List<String> amenities;
	private String hotelName;
	private String roomNumber;
	private List<String> imageUrl;

	// Constructors
	public RoomResponseDTO() {
	}

	public RoomResponseDTO(Long id, RoomType roomType, String description, Double price, boolean isAvailable,
			int maxGuests, List<String> amenities, String hotelName) {
		this.id = id;
		this.roomType = roomType;
		this.description = description;
		this.price = price;
		this.isAvailable = isAvailable;
		this.maxGuests = maxGuests;
		this.amenities = amenities;
		this.hotelName = hotelName;
	}

	public Long getId() {
		return id;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public List<String> getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(List<String> imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RoomType getRoomType() {
		return roomType;
	}

	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public int getMaxGuests() {
		return maxGuests;
	}

	public void setMaxGuests(int maxGuests) {
		this.maxGuests = maxGuests;
	}

	public List<String> getAmenities() {
		return amenities;
	}

	public void setAmenities(List<String> amenities) {
		this.amenities = amenities;
	}

	public String getHotelName() {
		return hotelName;
	}

	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}

}
