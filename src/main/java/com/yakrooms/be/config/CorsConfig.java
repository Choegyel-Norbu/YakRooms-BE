package com.yakrooms.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	 public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // More specific than /** for better security
                .allowedOrigins("http://localhost:5173","https://sdp-fe-xi.vercel.app", "https://2076-119-2-125-165.ngrok-free.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);  // 1 hour cache for preflight responses
    }
}
