package com.yakrooms.be.projection;

import java.time.LocalDateTime;
import java.util.Set;
import com.yakrooms.be.model.enums.HotelType;

public interface HotelWithCollectionsAndRatingProjection {
    Long getId();
    String getName();
    String getEmail();
    String getPhone();
    String getAddress();
    String getDistrict();
    String getLogoUrl();
    String getDescription();
    boolean getIsVerified();
    String getWebsiteUrl();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getLicenseUrl();
    String getIdProofUrl();
    String getLatitude();
    String getLongitude();
    HotelType getHotelType();
    Set<String> getAmenities();
    Set<String> getPhotoUrls();
    String getPhotoUrl();
    Double getAverageRating();
} 