package com.yakrooms.be.dto.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yakrooms.be.model.enums.HotelType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cache DTO for hotel search results - optimized for Redis serialization
 * Contains fields needed for search result display without JPA/Hibernate dependencies
 */
public class HotelSearchCacheDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("district")
    private String district;
    
    @JsonProperty("locality")
    private String locality;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("isVerified")
    private boolean isVerified;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("photoUrls")
    private List<String> photoUrls;
    
    @JsonProperty("amenities")
    private List<String> amenities;
    
    @JsonProperty("hotelType")
    private HotelType hotelType;
    
    @JsonProperty("lowestPrice")
    private Double lowestPrice;
    
    @JsonProperty("averageRating")
    private Double averageRating;

    // Default constructor for Jackson
    public HotelSearchCacheDto() {}

    // Constructor for easy creation
    public HotelSearchCacheDto(Long id, String name, String address, String district, String locality,
                              String description, String phone, boolean isVerified, LocalDateTime createdAt,
                              List<String> photoUrls, List<String> amenities, HotelType hotelType,
                              Double lowestPrice, Double averageRating) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.district = district;
        this.locality = locality;
        this.description = description;
        this.phone = phone;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.photoUrls = photoUrls;
        this.amenities = amenities;
        this.hotelType = hotelType;
        this.lowestPrice = lowestPrice;
        this.averageRating = averageRating;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public HotelType getHotelType() { return hotelType; }
    public void setHotelType(HotelType hotelType) { this.hotelType = hotelType; }

    public Double getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(Double lowestPrice) { this.lowestPrice = lowestPrice; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}
