package com.yakrooms.be.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import org.hibernate.annotations.BatchSize;

import com.yakrooms.be.model.enums.RoomType;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "room", indexes = {
    @Index(name = "idx_room_hotel_id", columnList = "hotel_id"),
    @Index(name = "idx_room_number_hotel", columnList = "room_number,hotel_id", unique = true),
    @Index(name = "idx_room_type", columnList = "room_type"),
    @Index(name = "idx_room_available", columnList = "is_available"),
    @Index(name = "idx_room_price", columnList = "price"),
    @Index(name = "idx_room_hotel_available", columnList = "hotel_id,is_available"),
    @Index(name = "idx_room_created_at", columnList = "created_at")
})

public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key - frequently used in queries
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    // Unique identifier within hotel - part of unique index
    @Column(name = "room_number", nullable = false, length = 50, unique = false)
    private String roomNumber;

    // Frequently filtered columns
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 50)
    private RoomType roomType;
    
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    // Price - used in sorting and filtering
    @Column(name = "price", nullable = false)
    private Double price;

    // Capacity - used in searches
    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    // Descriptive field - less frequently queried
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Collections - separate tables
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "room_amenities", 
        joinColumns = @JoinColumn(name = "room_id"),
        indexes = @Index(name = "idx_room_amenity_room_id", columnList = "room_id")
    )
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "amenity", length = 100)
    private List<String> amenities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "room_image_urls", 
        joinColumns = @JoinColumn(name = "room_id"),
        indexes = @Index(name = "idx_room_image_room_id", columnList = "room_id")
    )
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "url", length = 500)
    private List<String> imageUrl = new ArrayList<>();

    // One-to-Many relationships
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<RoomItem> items = new ArrayList<>();

    // Constructors
    public Room() {
        super();
    }

    public Room(RoomType roomType, String description, Double price, String roomNumber, 
                Hotel hotel, int maxGuests) {
        this.roomType = roomType;
        this.description = description;
        this.price = price;
        this.roomNumber = roomNumber;
        this.hotel = hotel;
        this.maxGuests = maxGuests;
        this.isAvailable = true;
    }

    // Lifecycle callbacks
    @PrePersist
    private void prePersist() {
        if (roomNumber != null) {
            roomNumber = roomNumber.trim().toUpperCase();
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (roomNumber != null) {
            roomNumber = roomNumber.trim().toUpperCase();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
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

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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



    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities != null ? amenities : new ArrayList<>();
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : new ArrayList<>();
    }

    public List<RoomItem> getItems() {
        return items;
    }

    public void setItems(List<RoomItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    // Helper methods for collection management
    public void addAmenity(String amenity) {
        if (amenity != null && !amenity.trim().isEmpty()) {
            this.amenities.add(amenity.trim());
        }
    }

    public void removeAmenity(String amenity) {
        this.amenities.remove(amenity);
    }

    public void addImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrl.add(imageUrl.trim());
        }
    }

    public void removeImageUrl(String imageUrl) {
        this.imageUrl.remove(imageUrl);
    }

    public void addItem(RoomItem item) {
        if (item != null) {
            this.items.add(item);
            item.setRoom(this);
        }
    }

    public void removeItem(RoomItem item) {
        if (item != null) {
            this.items.remove(item);
            item.setRoom(null);
        }
    }

    // Business logic methods
    public boolean hasAmenity(String amenity) {
        return amenities.contains(amenity);
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public boolean hasImages() {
        return !imageUrl.isEmpty();
    }

    public boolean hasAmenities() {
        return !amenities.isEmpty();
    }

    public boolean isAffordable(Double maxPrice) {
        return price != null && maxPrice != null && price <= maxPrice;
    }

    public boolean canAccommodate(int numberOfGuests) {
        return maxGuests >= numberOfGuests;
    }

    // toString, equals, and hashCode
    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomType=" + roomType +
                ", roomNumber='" + roomNumber + '\'' +
                ", price=" + price +
                ", isAvailable=" + isAvailable +
                ", maxGuests=" + maxGuests +
                ", hotelId=" + (hotel != null ? hotel.getId() : null) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}