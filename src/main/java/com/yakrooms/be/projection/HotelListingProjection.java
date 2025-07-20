package com.yakrooms.be.projection;

import java.time.LocalDateTime;

public interface HotelListingProjection {
    Long getId();
    String getName();
    String getAddress();
    String getDistrict();
    String getDescription();
    String getPhone();
    Boolean getIsVerified();
    String getPhotoUrl();      // from hotel_photo_urls
    String getAmenity();       // from hotel_amenities
    LocalDateTime getCreatedAt();
}
