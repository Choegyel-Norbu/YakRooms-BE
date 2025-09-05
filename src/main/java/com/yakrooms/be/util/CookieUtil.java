package com.yakrooms.be.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for secure cookie management
 * 
 * Security considerations:
 * - HttpOnly cookies prevent XSS attacks
 * - Secure flag ensures HTTPS-only transmission
 * - SameSite=Strict prevents CSRF attacks
 * - Proper path and domain settings
 * - Automatic expiration handling
 */
@Component
public class CookieUtil {
    
    // Cookie names
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    
    // Cookie configuration
    private final boolean secureCookies;
    private final String cookieDomain;
    
    public CookieUtil(@Value("${app.cookies.secure:true}") boolean secureCookies,
                     @Value("${app.cookies.domain:}") String cookieDomain) {
        this.secureCookies = secureCookies;
        this.cookieDomain = cookieDomain;
    }
    
    /**
     * Create a secure HttpOnly cookie for access token
     * Path: "/", Max-Age: 15 minutes, SameSite: Strict
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        Cookie cookie = createSecureCookie(ACCESS_TOKEN_COOKIE, token, maxAgeSeconds);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    /**
     * Create a secure HttpOnly cookie for refresh token
     * Path: "/refresh-token", Max-Age: 7 days, SameSite: Strict
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        Cookie cookie = createSecureCookie(REFRESH_TOKEN_COOKIE, token, maxAgeSeconds);
        cookie.setPath("/refresh-token");
        response.addCookie(cookie);
    }
    
    /**
     * Create a secure cookie with proper security attributes
     */
    private Cookie createSecureCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // Prevent XSS attacks
        cookie.setSecure(secureCookies); // HTTPS only in production
        cookie.setMaxAge(maxAgeSeconds);
        
        // Set domain if configured
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }
        
        // SameSite attribute is handled by Spring Security configuration
        return cookie;
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
        clearCookie(response, ACCESS_TOKEN_COOKIE, "/");
    }
    
    /**
     * Clear refresh token cookie
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        clearCookie(response, REFRESH_TOKEN_COOKIE, "/refresh-token");
    }
    
    /**
     * Clear both access and refresh token cookies
     */
    public void clearAllTokenCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }
    
    /**
     * Clear a specific cookie by setting max-age to 0
     */
    private void clearCookie(HttpServletResponse response, String cookieName, String path) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setMaxAge(0); // Expire immediately
        cookie.setPath(path);
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }
        
        response.addCookie(cookie);
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
