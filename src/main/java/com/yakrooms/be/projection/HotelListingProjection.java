package com.yakrooms.be.projection;

import java.time.LocalDateTime;

public interface HotelListingProjection {
    Long getId();
    String getName();
    String getAddress();
    String getDistrict();
    String getLocality();
    String getDescription();
    String getPhone();
    Boolean getIsVerified();
    LocalDateTime getCreatedAt();
    String getHotelType();
    String getPhotoUrls();
    String getPhotoUrl();
    String getAmenities();
}