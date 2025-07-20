package com.yakrooms.be.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.yakrooms.be.model.enums.HotelType;

public class HotelListingDto {
	private Long id;
	private String name;
	private String address;
	private String district;
	private String description;
	private String phone;
	private Boolean isVerified;
	private LocalDateTime createdAt;
	private List<String> photoUrls;
	private List<String> amenities;
	private String hotelType;

	public HotelListingDto(Long id, String name, String address, String district, String description, String phone,
			Boolean isVerified, LocalDateTime createdAt, String photoUrlsStr, String amenitiesStr, String hotelType) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.district = district;
		this.description = description;
		this.phone = phone;
		this.isVerified = isVerified;
		this.createdAt = createdAt;
		this.photoUrls = photoUrlsStr != null ? Arrays.asList(photoUrlsStr.split(",")) : Collections.emptyList();
		this.amenities = amenitiesStr != null ? Arrays.asList(amenitiesStr.split(",")) : Collections.emptyList();
		this.hotelType = hotelType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getHotelType() {
		return hotelType;
	}

	public void setHotelType(String hotelType) {
		this.hotelType = hotelType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<String> getPhotoUrls() {
		return photoUrls;
	}

	public void setPhotoUrls(List<String> photoUrls) {
		this.photoUrls = photoUrls;
	}

	public List<String> getAmenities() {
		return amenities;
	}

	public void setAmenities(List<String> amenities) {
		this.amenities = amenities;
	}

}
