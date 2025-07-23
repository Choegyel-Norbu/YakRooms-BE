package com.yakrooms.be.projection;

import java.time.LocalDateTime;
import java.util.List;
import com.yakrooms.be.model.enums.HotelType;

public interface HotelWithLowestPriceProjection {
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
    List<String> getPhotoUrls();
    String getLicenseUrl();
    String getIdProofUrl();
    List<String> getAmenities();
    HotelType getHotelType();
    Double getLowestPrice();
    String getPhotoUrl();

} 