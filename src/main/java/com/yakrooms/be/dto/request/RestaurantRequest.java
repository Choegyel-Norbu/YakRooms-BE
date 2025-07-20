package com.yakrooms.be.dto.request;

public class RestaurantRequest {
	public String name;
	public String address;
	public String district;
	public String description;
	public String logoUrl;
	public Long hotelId;

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}
}