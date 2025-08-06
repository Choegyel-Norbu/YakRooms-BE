package com.yakrooms.be.projection;

import java.time.LocalDateTime;

public interface HotelWithRatingProjection {
    Long getId();
    String getName();
    String getEmail();
    String getPhone();
    String getAddress();
    String getDistrict();
    String getLogoUrl();
    String getDescription();
    Boolean getIsVerified();
    String getWebsiteUrl();
    LocalDateTime getCreatedAt();
    String getLicenseUrl();
    String getIdProofUrl();
    Double getAvgRating();
    Long getReviewCount();
} 