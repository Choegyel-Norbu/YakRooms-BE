package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;

public class ReviewResponse {
    public Long id;
    public int rating;
    public String comment;
    public Long userId;
    public LocalDateTime createdAt;
}