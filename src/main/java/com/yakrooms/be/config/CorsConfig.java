package com.yakrooms.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns( // Use allowedOriginPatterns instead
				"http://localhost:*", 
				"https://localhost:*",
				"http://127.0.0.1:*",
				"https://yak-rooms-fe.vercel.app",
				"https://yak-rooms-fe-main.vercel.app",
				"https://0d123863c798.ngrok-free.app",
				"https://*.ngrok-free.app", // Pattern for any ngrok subdomain
				"https://*.vercel.app" // Pattern for any vercel deployment
			)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
			.allowedHeaders("*")
			.exposedHeaders("Authorization", "Content-Type", "X-Requested-With", "X-XSRF-TOKEN", "X-CSRF-TOKEN")
			.allowCredentials(true)
			.maxAge(3600);
	}

}
