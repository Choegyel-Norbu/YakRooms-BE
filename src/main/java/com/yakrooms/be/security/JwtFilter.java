package com.yakrooms.be.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yakrooms.be.model.enums.Role;

public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(7);
            if (JwtUtil.validateToken(token)) {
                // Extract user information from JWT
                String email = JwtUtil.extractEmail(token);
                Long userId = JwtUtil.extractUserId(token);
                String rolesString = JwtUtil.extractRoles(token);
                
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
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Log the exception but don't fail the request
            // Spring Security will handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }
}
