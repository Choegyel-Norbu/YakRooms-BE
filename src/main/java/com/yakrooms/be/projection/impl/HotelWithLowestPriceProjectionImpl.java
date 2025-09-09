package com.yakrooms.be.projection.impl;

import com.yakrooms.be.model.enums.HotelType;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Concrete implementation of HotelWithLowestPriceProjection
 * Used for converting cached DTOs back to projections without JPA proxy issues
 */
public class HotelWithLowestPriceProjectionImpl implements HotelWithLowestPriceProjection {
    
    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final String address;
    private final String district;
    private final String locality;
    private final String logoUrl;
    private final String description;
    private final Boolean isVerified;
    private final String websiteUrl;
    private final LocalDateTime createdAt;
    private final List<String> photoUrls;
    private final String licenseUrl;
    private final String idProofUrl;
    private final List<String> amenities;
    private final HotelType hotelType;
    private final Double lowestPrice;
    private final String photoUrl;
    private final Double averageRating;

    public HotelWithLowestPriceProjectionImpl(Long id, String name, String email, String phone, String address,
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

    @Override
    public Long getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getEmail() { return email; }

    @Override
    public String getPhone() { return phone; }

    @Override
    public String getAddress() { return address; }

    @Override
    public String getDistrict() { return district; }

    @Override
    public String getLocality() { return locality; }

    @Override
    public String getLogoUrl() { return logoUrl; }

    @Override
    public String getDescription() { return description; }

    @Override
    public boolean getIsVerified() { return isVerified; }

    @Override
    public String getWebsiteUrl() { return websiteUrl; }

    @Override
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public List<String> getPhotoUrls() { return photoUrls; }

    @Override
    public String getLicenseUrl() { return licenseUrl; }

    @Override
    public String getIdProofUrl() { return idProofUrl; }

    @Override
    public List<String> getAmenities() { return amenities; }

    @Override
    public HotelType getHotelType() { return hotelType; }

    @Override
    public Double getLowestPrice() { return lowestPrice; }

    @Override
    public String getPhotoUrl() { return photoUrl; }

    @Override
    public Double getAverageRating() { return averageRating; }
}