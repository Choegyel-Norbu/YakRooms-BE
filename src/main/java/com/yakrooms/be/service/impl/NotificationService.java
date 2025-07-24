package com.yakrooms.be.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.NotificationMessage;

@Service
public class NotificationService {

	/**
     * The SimpMessagingTemplate is a core Spring component for sending STOMP messages.
     * It's pre-configured by Spring Boot and can be injected directly into any Spring bean.
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor-based dependency injection is used to provide an instance of SimpMessagingTemplate.
     * This is the recommended way to inject dependencies in Spring.
     *
     * @param messagingTemplate The template for sending messages to STOMP destinations.
     */
    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a notification payload to a specific user's topic.
     * This method can be called from any part of your application (e.g., BookingService, PaymentService)
     * whenever a real-time notification is required.
     *
     * @param userId  The unique identifier for the user who should receive the notification.
     * This is used to construct the specific topic destination. It must be a String.
     * @param payload The NotificationMessage object containing the data to be sent.
     * This object will be automatically serialized to JSON by Spring.
     */
    public void notifyUser(String userId, NotificationMessage payload) {
        // Construct the destination topic string. This is the "channel" the client
        // needs to be subscribed to. For example, if userId is "admin-hotel-123",
        // the destination will be "/topic/notifications/admin-hotel-123".
        String destination = "/topic/notifications/" + userId;

        // The convertAndSend method takes the destination and the payload.
        // It handles the conversion of the Java object (payload) into a message
        // (typically JSON) and sends it to the message broker, which then pushes
        // it to all subscribed clients on that destination.
        messagingTemplate.convertAndSend(destination, payload);
    }
}
