package com.yakrooms.be.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yakrooms.be.model.enums.RoomType;

import jakarta.persistence.*;

@Entity
public class Room {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private RoomType roomType;
	private String description;
	private Double price;
	private boolean isAvailable;
	private String roomNumber;

	@ManyToOne
	@JoinColumn(name = "hotel_id")
	private Hotel hotel;

	private int maxGuests;

	@ElementCollection
	@CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
	@Column(name = "amenity")
	private List<String> amenities = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "room_image_urls", joinColumns = @JoinColumn(name = "room_id"))
	@Column(name = "url")
	private List<String> imageUrl = new ArrayList<>();

	@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RoomItem> items = new ArrayList<>();

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public Room() {
		super();
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

	public void setId(Long id) {
		this.id = id;
	}

	public RoomType getRoomType() {
		return roomType;
	}

	public boolean getAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public List<String> getAmenities() {
		return amenities;
	}

	public void setAmenities(List<String> amenities) {
		this.amenities = amenities;
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

	public List<RoomItem> getItems() {
		return items;
	}

	public void setItems(List<RoomItem> items) {
		this.items = items;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public List<String> getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(List<String> imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public int getMaxGuests() {
		return maxGuests;
	}

	public void setMaxGuests(int maxGuests) {
		this.maxGuests = maxGuests;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
