package com.yakrooms.be.projection;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    LocalTime getCheckinTime();
    LocalTime getCheckoutTime();
    String getPhotoUrls();
    String getPhotoUrl();
    String getAmenities();
}