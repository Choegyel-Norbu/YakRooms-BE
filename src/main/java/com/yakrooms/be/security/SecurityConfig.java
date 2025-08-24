package com.yakrooms.be.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.and())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/auth/firebase").permitAll()
                .requestMatchers("/api/hotels/topThree").permitAll()
                .requestMatchers("/api/hotels/details/**").permitAll()
                .requestMatchers("/api/rooms/available/**").permitAll()
                .requestMatchers("/api/reviews/hotel/*/testimonials/paginated").permitAll()
                .requestMatchers("/api/reviews/averageRating").permitAll()
                .requestMatchers("/api/getIntouch").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/api/upload").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
