package com.yakrooms.be.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		logger.info("Configuring WebSocket message broker");
		
		// Enable simple broker for sending messages to clients
		config.enableSimpleBroker("/topic", "/queue");
		logger.info("Enabled simple broker for topics: /topic, /queue");

		// Set application destination prefix for client messages
		config.setApplicationDestinationPrefixes("/app");
		logger.info("Set application destination prefix: /app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		logger.info("Registering STOMP endpoints");
		
		// Main WebSocket endpoint - clients should connect to this
		registry.addEndpoint("/ws")
				.setAllowedOriginPatterns("*") // For development - be more specific in production
				.withSockJS(); // Enable SockJS fallback
		logger.info("Registered STOMP endpoint: /ws with SockJS support");
		
		// Alternative endpoint specifically for bookings if needed
		registry.addEndpoint("/ws/bookings")
				.setAllowedOriginPatterns("*")
				.withSockJS();
		logger.info("Registered STOMP endpoint: /ws/bookings with SockJS support");
		
		logger.info("WebSocket endpoints registered successfully");
	}

	@Override
	public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
		logger.info("Configuring WebSocket message converters");
		
		DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
		resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setObjectMapper(new ObjectMapper());
		converter.setContentTypeResolver(resolver);

		messageConverters.add(converter);
		logger.info("Added MappingJackson2MessageConverter to WebSocket message converters");
		
		return false; // Don't add default converters
	}
}