package com.yakrooms.be.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yakrooms.be.util.CookieUtil;

public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private CookieUtil cookieUtil;
    
    @Autowired
    private JwtUtil jwtUtil;

    // List of public endpoints that don't require JWT processing (MORE SPECIFIC FIRST)
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/auth/firebase",
        "/auth/refresh-token",
        "/auth/logout",
        "/api/hotels/list",
        "/api/hotels/topThree",
        "/api/hotels/details",
        "/api/hotels/search",
        "/api/hotels/sortedByLowestPrice",
        "/api/hotels/sortedByHighestPrice",
        "/api/rooms/available",
        "/api/rooms/*/booked-dates",
        "/api/reviews/hotel",
        "/api/reviews/averageRating",
        "/api/getIntouch",
        "/api/v1/uploadthing/health",
        "/actuator/health",
        "/health",
        "/health/ping",
        "/health/ready",
        "/health/db",
        
        // WebSocket endpoints - Public access for SockJS
        "/ws",
        "/ws/info",
        "/ws/websocket",
        "/ws/xhr_streaming",
        "/ws/xhr_send",
        "/error",
        "/favicon.ico",
        "/robots.txt"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        
        // Skip JWT processing for public endpoints
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (isPublicEndpoint(requestURI, publicEndpoint)) {
                logger.debug("Skipping JWT filter for public endpoint: " + requestURI);
                return true; // Skip filtering for this request
            }
        }
        
        logger.debug("Processing JWT filter for endpoint: " + requestURI);
        return false; // Process JWT for this request
    }
    
    /**
     * Check if the request URI matches a public endpoint pattern
     */
    private boolean isPublicEndpoint(String requestURI, String publicEndpoint) {
        // Exact match
        if (requestURI.equals(publicEndpoint)) {
            return true;
        }
        
        // Starts with pattern (for endpoints with path parameters)
        if (requestURI.startsWith(publicEndpoint + "/")) {
            return true;
        }
        
        // Special case for root endpoints
        if (publicEndpoint.endsWith("/") && requestURI.startsWith(publicEndpoint)) {
            return true;
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Try to get access token from cookie first (preferred method)
        String token = cookieUtil.getAccessTokenFromCookie(request);
        System.out.println("DEBUG: JwtFilter - Token from cookie: " + (token != null ? "Found (length: " + token.length() + ")" : "Not found"));
        
        // Fallback to Authorization header for backward compatibility
        if (token == null) {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                System.out.println("DEBUG: JwtFilter - Token from header: " + (token != null ? "Found (length: " + token.length() + ")" : "Not found"));
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate access token
            if (jwtUtil.validateAccessToken(token)) {
                // Extract user information from JWT
                String email = jwtUtil.extractEmail(token);
                Long userId = jwtUtil.extractUserId(token);
                String rolesString = jwtUtil.extractRoles(token);
                
                // Convert roles string to authorities
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (rolesString != null && !rolesString.isEmpty()) {
                    String[] roles = rolesString.split(",");
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
                
                // Set additional details
                authentication.setDetails(new JwtAuthenticationDetails(userId, token));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("Successfully authenticated user: " + email);
            } else {
                logger.debug("Invalid or expired access token");
            }
        } catch (Exception e) {
            // Log the exception but don't fail the request
            // Spring Security will handle unauthorized access
            logger.warn("JWT token validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
