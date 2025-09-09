package com.yakrooms.be.dto.response;

import com.yakrooms.be.model.enums.HotelType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Clean response DTO for hotel listings that can be safely serialized
 * Used when returning cached data to avoid JPA proxy serialization issues
 */
public class HotelListingResponseDto {
    
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
    private Boolean isVerified;
    
    @JsonProperty("websiteUrl")
    private String websiteUrl;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("photoUrls")
    private List<String> photoUrls;
    
    @JsonProperty("licenseUrl")
    private String licenseUrl;
    
    @JsonProperty("idProofUrl")
    private String idProofUrl;
    
    @JsonProperty("amenities")
    private List<String> amenities;
    
    @JsonProperty("hotelType")
    private HotelType hotelType;
    
    @JsonProperty("lowestPrice")
    private Double lowestPrice;
    
    @JsonProperty("photoUrl")
    private String photoUrl;
    
    @JsonProperty("averageRating")
    private Double averageRating;

    // Default constructor
    public HotelListingResponseDto() {}

    // Constructor with all fields
    public HotelListingResponseDto(Long id, String name, String email, String phone, String address,
                                 String district, String locality, String logoUrl, String description,
                                 Boolean isVerified, String websiteUrl, LocalDateTime createdAt,
                                 List<String> photoUrls, String licenseUrl, String idProofUrl,
                                 List<String> amenities, HotelType hotelType, Double lowestPrice,
                                 String photoUrl, Double averageRating) {
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
        this.photoUrls = photoUrls;
        this.licenseUrl = licenseUrl;
        this.idProofUrl = idProofUrl;
        this.amenities = amenities;
        this.hotelType = hotelType;
        this.lowestPrice = lowestPrice;
        this.photoUrl = photoUrl;
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

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }

    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }

    public String getIdProofUrl() { return idProofUrl; }
    public void setIdProofUrl(String idProofUrl) { this.idProofUrl = idProofUrl; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public HotelType getHotelType() { return hotelType; }
    public void setHotelType(HotelType hotelType) { this.hotelType = hotelType; }

    public Double getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(Double lowestPrice) { this.lowestPrice = lowestPrice; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    @Override
    public String toString() {
        return "HotelListingResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", district='" + district + '\'' +
                ", locality='" + locality + '\'' +
                ", isVerified=" + isVerified +
                ", lowestPrice=" + lowestPrice +
                ", averageRating=" + averageRating +
                '}';
    }
}
