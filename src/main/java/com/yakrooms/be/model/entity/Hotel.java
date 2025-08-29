package com.yakrooms.be.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;


import com.yakrooms.be.model.enums.HotelType;
import com.yakrooms.be.model.entity.Review;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "hotels", indexes = {
    @Index(name = "idx_hotel_email", columnList = "email"),
    @Index(name = "idx_hotel_district", columnList = "district"),
    @Index(name = "idx_hotel_locality", columnList = "locality"),
    @Index(name = "idx_hotel_verified", columnList = "is_verified"),
    @Index(name = "idx_hotel_type", columnList = "hotel_type"),
    @Index(name = "idx_hotel_district_type_verified", columnList = "district,hotel_type,is_verified"),
    @Index(name = "idx_hotel_district_locality", columnList = "district,locality")
})

public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String locality;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "license_url", length = 500)
    private String licenseUrl;

    @Column(name = "id_proof_url", length = 500)
    private String idProofUrl;

    @Column(length = 50)
    private String latitude;

    @Column(length = 50)
    private String longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "hotel_type", length = 50)
    private HotelType hotelType;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private Set<User> users = new HashSet<>();

    @OneToOne(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 20)
    private Set<Staff> staffList = new HashSet<>();

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<Room> rooms = new HashSet<>();

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private Set<Review> reviews = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "hotel_amenities", 
        joinColumns = @JoinColumn(name = "hotel_id"),
        indexes = @Index(name = "idx_amenity_hotel_id", columnList = "hotel_id")
    )
    @Column(name = "amenity", length = 100)
    @BatchSize(size = 20)
    private Set<String> amenities = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "hotel_photo_urls", 
        joinColumns = @JoinColumn(name = "hotel_id"),
        indexes = @Index(name = "idx_photo_hotel_id", columnList = "hotel_id")
    )
    @Column(name = "url", length = 500)
    @BatchSize(size = 20)
    private Set<String> photoUrls = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    // Constructors
    public Hotel() {
        this.users = new HashSet<>();
        this.staffList = new HashSet<>();
        this.rooms = new HashSet<>();
        this.amenities = new HashSet<>();
        this.photoUrls = new HashSet<>();
        this.isVerified = false;
    }

    public Hotel(String name, String email, String phone, String address, String district, String locality) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.district = district;
        this.locality = locality;
    }

    // Lifecycle callbacks
    @PrePersist
    private void prePersist() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
        if (district != null) {
            district = district.trim();
        }
        if (locality != null) {
            locality = locality.trim();
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
        if (district != null) {
            district = district.trim();
        }
        if (locality != null) {
            locality = locality.trim();
        }
    }

    // Utility methods for relationship management
    public void addUser(User user) {
        users.add(user);
        user.setHotel(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setHotel(null);
    }

    public void addRoom(Room room) {
        rooms.add(room);
        room.setHotel(this);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        room.setHotel(null);
    }

    public void addStaff(Staff staff) {
        staffList.add(staff);
        staff.setHotel(this);
    }

    public void removeStaff(Staff staff) {
        staffList.remove(staff);
        staff.setHotel(null);
    }

    public void addAmenity(String amenity) {
        if (amenity != null && !amenity.trim().isEmpty()) {
            amenities.add(amenity.trim());
        }
    }

    public void removeAmenity(String amenity) {
        amenities.remove(amenity);
    }

    public void addPhotoUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            photoUrls.add(url.trim());
        }
    }

    public void removePhotoUrl(String url) {
        photoUrls.remove(url);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getIdProofUrl() {
        return idProofUrl;
    }

    public void setIdProofUrl(String idProofUrl) {
        this.idProofUrl = idProofUrl;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public HotelType getHotelType() {
        return hotelType;
    }

    public void setHotelType(HotelType hotelType) {
        this.hotelType = hotelType;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        if (restaurant != null) {
            restaurant.setHotel(this);
        }
    }

    public Set<Staff> getStaffList() {
        return staffList;
    }

    public void setStaffList(Set<Staff> staffList) {
        this.staffList = staffList;
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<String> amenities) {
        this.amenities = amenities;
    }

    public Set<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(Set<String> photoUrls) {
        this.photoUrls = photoUrls;
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



    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hotel hotel = (Hotel) o;
        return Objects.equals(id, hotel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", district='" + district + '\'' +
                ", locality='" + locality + '\'' +
                ", hotelType=" + hotelType +
                ", isVerified=" + isVerified +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}