package com.yakrooms.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOriginPatterns( // Use allowedOriginPatterns instead
				"http://localhost:5173", "https://yak-rooms-fe.vercel.app", "https://0d123863c798.ngrok-free.app",
				"https://*.ngrok-free.app" // Pattern for any ngrok subdomain
		).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH").allowedHeaders("*")
				.exposedHeaders("Authorization").allowCredentials(true).maxAge(3600);
	}

}
