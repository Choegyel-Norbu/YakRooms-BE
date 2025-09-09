package com.yakrooms.be.dto.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yakrooms.be.model.enums.HotelType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cache DTO for hotel details - optimized for Redis serialization
 * Contains all fields needed for hotel detail display without JPA/Hibernate dependencies
 */
public class HotelDetailsCacheDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("district")
    private String district;
    
    @JsonProperty("locality")
    private String locality;
    
    @JsonProperty("logoUrl")
    private String logoUrl;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("isVerified")
    private boolean isVerified;
    
    @JsonProperty("websiteUrl")
    private String websiteUrl;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("licenseUrl")
    private String licenseUrl;
    
    @JsonProperty("idProofUrl")
    private String idProofUrl;
    
    @JsonProperty("latitude")
    private String latitude;
    
    @JsonProperty("longitude")
    private String longitude;
    
    @JsonProperty("hotelType")
    private HotelType hotelType;
    
    @JsonProperty("amenities")
    private List<String> amenities;
    
    @JsonProperty("photoUrls")
    private List<String> photoUrls;
    
    @JsonProperty("averageRating")
    private Double averageRating;

    // Default constructor for Jackson
    public HotelDetailsCacheDto() {}

    // Constructor for easy creation
    public HotelDetailsCacheDto(Long id, String name, String email, String phone, String address,
                               String district, String locality, String logoUrl, String description,
                               boolean isVerified, String websiteUrl, LocalDateTime createdAt,
                               String licenseUrl, String idProofUrl, String latitude, String longitude,
                               HotelType hotelType, List<String> amenities, List<String> photoUrls,
                               Double averageRating) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.district = district;
        this.locality = locality;
        this.logoUrl = logoUrl;
        this.description = description;
        this.isVerified = isVerified;
        this.websiteUrl = websiteUrl;
        this.createdAt = createdAt;
        this.licenseUrl = licenseUrl;
        this.idProofUrl = idProofUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hotelType = hotelType;
        this.amenities = amenities;
        this.photoUrls = photoUrls;
        this.averageRating = averageRating;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }

    public String getIdProofUrl() { return idProofUrl; }
    public void setIdProofUrl(String idProofUrl) { this.idProofUrl = idProofUrl; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public HotelType getHotelType() { return hotelType; }
    public void setHotelType(HotelType hotelType) { this.hotelType = hotelType; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}
