package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;

public class MenuItemResponse {
    public Long id;
    public String name;
    public String category;
    public Double price;
    public String description;
    public boolean isAvailable;
    public LocalDateTime createdAt;
}