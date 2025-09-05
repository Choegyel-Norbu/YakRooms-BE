package com.yakrooms.be.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/auth/firebase", "/api/auth/refresh-token", "/api/auth/logout")
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(jwtAuthenticationProvider)
            .headers(headers -> headers
                // Prevent clickjacking attacks
                .frameOptions(frameOptions -> frameOptions.deny())
                // Prevent MIME type sniffing
                .contentTypeOptions(contentTypeOptions -> {})
                // Force HTTPS for 1 year (including subdomains)
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                )
                // Content Security Policy - restrict resource loading
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                                   "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                   "style-src 'self' 'unsafe-inline'; " +
                                   "img-src 'self' data: https:; " +
                                   "font-src 'self'; " +
                                   "connect-src 'self' ws: wss:; " +
                                   "frame-ancestors 'none';")
                )
                // Additional security headers
                .xssProtection(xss -> xss.disable())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - No authentication required (MORE SPECIFIC FIRST)
                .requestMatchers("/auth/firebase").permitAll()
                .requestMatchers("/auth/refresh-token").permitAll()
                .requestMatchers("/auth/logout").permitAll()
                .requestMatchers("/api/hotels/list").permitAll()
                .requestMatchers("/api/hotels/topThree").permitAll()
                .requestMatchers("/api/hotels/details/**").permitAll()
                .requestMatchers("/api/hotels/search").permitAll()
                .requestMatchers("/api/hotels/sortedByLowestPrice").permitAll()
                .requestMatchers("/api/hotels/sortedByHighestPrice").permitAll()
                .requestMatchers("/api/rooms/available/**").permitAll()
                .requestMatchers("/api/rooms/*/booked-dates").permitAll()
                .requestMatchers("/api/reviews/hotel/*/testimonials/paginated").permitAll()
                .requestMatchers("/api/reviews/averageRating").permitAll()
                .requestMatchers("/api/getIntouch").permitAll()
                
                // Health check endpoints - Public access for monitoring
                .requestMatchers("/api/v1/uploadthing/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/health/ping").permitAll()
                .requestMatchers("/health/ready").permitAll()
                .requestMatchers("/health/db").permitAll()
                
                // WebSocket endpoints - Public access for SockJS
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws").permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (your frontend domains)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",         // Local development servers
            "https://localhost:*",        // Local HTTPS development
            "http://127.0.0.1:*",        // Local IP development
            "https://yak-rooms-fe.vercel.app",        // Vercel main deployment
            "https://yak-rooms-fe-main.vercel.app",   // Vercel branch deployment
            "https://*.vercel.app",                   // Pattern for any Vercel deployment
            "https://*.ngrok-free.app"                // Pattern for any ngrok subdomain
        ));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allow specific headers (including CSRF tokens)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-XSRF-TOKEN",  // Spring Security CSRF token header
            "X-CSRF-TOKEN"  // Alternative CSRF token header
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Set max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);
        
        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
