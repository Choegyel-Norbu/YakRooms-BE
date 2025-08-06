package com.yakrooms.be.dto.response;

import java.time.LocalDateTime;

public class ReviewResponse {
    public Long id;
    public int rating;
    public String comment;
    public Long userId;
    public String userName;
    public String userEmail;
    public String userProfilePicUrl;
    public LocalDateTime createdAt;
}