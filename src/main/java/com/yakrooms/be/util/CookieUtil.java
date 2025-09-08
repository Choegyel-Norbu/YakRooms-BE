package com.yakrooms.be.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for secure cookie management with smart SameSite strategy
 * 
 * Security considerations:
 * - HttpOnly cookies prevent XSS attacks
 * - Secure flag ensures HTTPS-only transmission in production
 * - Smart SameSite strategy:
 *   * Production (HTTPS): SameSite=None for cross-domain requests
 *   * Development (HTTP): SameSite=Lax for same-domain requests
 * - Proper path and domain settings
 * - Automatic expiration handling
 * 
 * SameSite Strategy:
 * - SameSite=None requires Secure=true (HTTPS) to work in modern browsers
 * - In development (HTTP), we use SameSite=Lax which still allows cookies to work
 * - In production (HTTPS), we use SameSite=None for full cross-domain support
 */
@Component
public class CookieUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);
    
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
        
        // Log cookie configuration for debugging
        logger.info("CookieUtil initialized - secureCookies: {}, domain: '{}'", 
                   secureCookies, cookieDomain);
    }
    
    /**
     * Create a secure HttpOnly cookie for access token
     * Path: "/", Max-Age: 15 minutes, SameSite: None (for cross-domain requests)
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        ResponseCookie cookie = createSecureResponseCookie(ACCESS_TOKEN_COOKIE, token, maxAgeSeconds);
        response.addHeader("Set-Cookie", cookie.toString());
    }
    
    /**
     * Create a secure HttpOnly cookie for refresh token
     * Path: "/", Max-Age: 7 days, SameSite: None (for cross-domain requests)
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        ResponseCookie cookie = createSecureResponseCookie(REFRESH_TOKEN_COOKIE, token, maxAgeSeconds);
        response.addHeader("Set-Cookie", cookie.toString());
    }
    
    /**
     * Create a secure ResponseCookie with proper security attributes including SameSite
     * 
     * SameSite Strategy:
     * - If Secure=true (HTTPS): Use SameSite=None for cross-domain requests
     * - If Secure=false (HTTP): Use SameSite=Lax for same-domain requests
     * 
     * This ensures cookies work in both development (HTTP) and production (HTTPS)
     */
    private ResponseCookie createSecureResponseCookie(String name, String value, int maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .path("/")
            .maxAge(maxAgeSeconds)
            .httpOnly(true) // Prevent XSS attacks
            .secure(secureCookies); // HTTPS only in production
        
        // Smart SameSite strategy based on security context
        String sameSiteValue;
        if (secureCookies) {
            // Production (HTTPS): Use SameSite=None for cross-domain requests
            sameSiteValue = "None";
            logger.debug("Using SameSite=None for production (HTTPS) - cookie: {}", name);
        } else {
            // Development (HTTP): Use SameSite=Lax for same-domain requests
            // This allows cookies to work in development while maintaining security
            sameSiteValue = "Lax";
            logger.debug("Using SameSite=Lax for development (HTTP) - cookie: {}", name);
        }
        builder.sameSite(sameSiteValue);
        
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
     * Uses the same smart SameSite strategy as cookie creation
     */
    private void clearResponseCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
            .path("/")
            .maxAge(0) // Expire immediately
            .httpOnly(true)
            .secure(secureCookies);
        
        // Smart SameSite strategy based on security context
        String sameSiteValue;
        if (secureCookies) {
            // Production (HTTPS): Use SameSite=None for cross-domain requests
            sameSiteValue = "None";
            logger.debug("Clearing cookie with SameSite=None for production (HTTPS) - cookie: {}", cookieName);
        } else {
            // Development (HTTP): Use SameSite=Lax for same-domain requests
            sameSiteValue = "Lax";
            logger.debug("Clearing cookie with SameSite=Lax for development (HTTP) - cookie: {}", cookieName);
        }
        builder.sameSite(sameSiteValue);
        
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
