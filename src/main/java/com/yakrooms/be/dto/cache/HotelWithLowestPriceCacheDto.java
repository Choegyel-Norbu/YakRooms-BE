package com.yakrooms.be.dto.cache;

import com.yakrooms.be.model.enums.HotelType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cache DTO for HotelWithLowestPriceProjection
 * Plain Java object for Redis serialization without JPA proxies
 */
public class HotelWithLowestPriceCacheDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String district;
    private String locality;
    private String logoUrl;
    private String description;
    private boolean isVerified;
    private String websiteUrl;
    private LocalDateTime createdAt;
    private List<String> photoUrls;
    private String licenseUrl;
    private String idProofUrl;
    private List<String> amenities;
    private HotelType hotelType;
    private Double lowestPrice;
    private String photoUrl;
    private Double averageRating;

    // Default constructor
    public HotelWithLowestPriceCacheDto() {}

    // Constructor with all fields
    public HotelWithLowestPriceCacheDto(Long id, String name, String email, String phone, String address,
                                       String district, String locality, String logoUrl, String description,
                                       boolean isVerified, String websiteUrl, LocalDateTime createdAt,
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

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

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
}
