package com.yakrooms.be.security;

public class JwtAuthenticationDetails {
    private final Long userId;
    private final String token;

    public JwtAuthenticationDetails(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }
}
