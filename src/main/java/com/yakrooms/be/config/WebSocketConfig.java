package com.yakrooms.be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Broker for pushing to client
        config.setApplicationDestinationPrefixes("/app"); // Client → Server endpoint prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket handshake endpoint
                .setAllowedOriginPatterns("*") // Allow all for testing
                .withSockJS(); // Fallback for browsers without WebSocket
    }
}