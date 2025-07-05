package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;

public class RestaurantResponse {
    public Long id;
    public String name;
    public String address;
    public String district;
    public String description;
    public String logoUrl;
    public boolean isVerified;
    public LocalDateTime createdAt;
}