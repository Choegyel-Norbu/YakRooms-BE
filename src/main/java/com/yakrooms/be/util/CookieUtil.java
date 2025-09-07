package com.yakrooms.be.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utility class for secure cookie management
 * 
 * Security considerations:
 * - HttpOnly cookies prevent XSS attacks
 * - Secure flag ensures HTTPS-only transmission
 * - SameSite policy configurable (None for production cross-site, Lax for development)
 * - Proper path and domain settings
 * - Automatic expiration handling
 * 
 * Note: SameSite=None requires Secure=true (HTTPS) for production safety
 * Development uses SameSite=Lax for better compatibility with localhost
 */
@Component
public class CookieUtil {
    
    // Cookie names
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    
    // Cookie configuration
    private final boolean secureCookies;
    private final String cookieDomain;
    private final String sameSite;
    
    public CookieUtil(@Value("${app.cookies.secure:true}") boolean secureCookies,
                     @Value("${app.cookies.domain:}") String cookieDomain,
                     @Value("${app.cookies.samesite:None}") String sameSite) {
        this.secureCookies = secureCookies;
        this.cookieDomain = cookieDomain;
        this.sameSite = sameSite;
    }
    
    /**
     * Create a secure HttpOnly cookie for access token
     * Path: "/", Max-Age: 15 minutes, SameSite: configurable (Lax for dev, None for prod)
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        ResponseCookie cookie = createSecureResponseCookie(ACCESS_TOKEN_COOKIE, token, maxAgeSeconds);
        response.addHeader("Set-Cookie", cookie.toString());
    }
    
    /**
     * Create a secure HttpOnly cookie for refresh token
     * Path: "/", Max-Age: 7 days, SameSite: configurable (Lax for dev, None for prod)
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        ResponseCookie cookie = createSecureResponseCookie(REFRESH_TOKEN_COOKIE, token, maxAgeSeconds);
        response.addHeader("Set-Cookie", cookie.toString());
    }
    
    /**
     * Create a secure ResponseCookie with proper security attributes including SameSite
     */
    private ResponseCookie createSecureResponseCookie(String name, String value, int maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .path("/")
            .maxAge(maxAgeSeconds)
            .httpOnly(true) // Prevent XSS attacks
            .secure(secureCookies) // HTTPS only in production
            .sameSite(sameSite); // Configurable SameSite policy
        
        // Set domain if configured
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }
        
        return builder.build();
    }
    
    
    /**
     * Extract access token from request cookies
     */
    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }
    
    /**
     * Extract refresh token from request cookies
     */
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }
    
    /**
     * Get cookie value by name
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * Clear access token cookie
     */
    public void clearAccessTokenCookie(HttpServletResponse response) {
        clearResponseCookie(response, ACCESS_TOKEN_COOKIE);
    }
    
    /**
     * Clear refresh token cookie
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        clearResponseCookie(response, REFRESH_TOKEN_COOKIE);
    }
    
    /**
     * Clear both access and refresh token cookies
     */
    public void clearAllTokenCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }
    
    /**
     * Clear a specific cookie using ResponseCookie (expires immediately)
     */
    private void clearResponseCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
            .path("/")
            .maxAge(0) // Expire immediately
            .httpOnly(true)
            .secure(secureCookies)
            .sameSite(sameSite); // Configurable SameSite policy
        
        // Set domain if configured
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }
        
        ResponseCookie cookie = builder.build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
    
    
    /**
     * Check if request has access token cookie
     */
    public boolean hasAccessTokenCookie(HttpServletRequest request) {
        return getAccessTokenFromCookie(request) != null;
    }
    
    /**
     * Check if request has refresh token cookie
     */
    public boolean hasRefreshTokenCookie(HttpServletRequest request) {
        return getRefreshTokenFromCookie(request) != null;
    }
}
