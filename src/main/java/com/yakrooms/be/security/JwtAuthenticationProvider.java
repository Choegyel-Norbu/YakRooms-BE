package com.yakrooms.be.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.yakrooms.be.model.enums.Role;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            String token = jwtAuth.getCredentials().toString();
            
            try {
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
                    
                    // Create authenticated token
                    UsernamePasswordAuthenticationToken authenticatedToken = 
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authenticatedToken.setDetails(new JwtAuthenticationDetails(userId, token));
                    
                    return authenticatedToken;
                } else {
                    throw new BadCredentialsException("Invalid JWT token");
                }
            } catch (Exception e) {
                throw new BadCredentialsException("JWT token validation failed: " + e.getMessage());
            }
        }
        
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
