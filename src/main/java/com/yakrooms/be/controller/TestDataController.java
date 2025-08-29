package com.yakrooms.be.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test-data")
public class TestDataController {

    // Public endpoint to check if test data is available
    @GetMapping("/status")
    public Map<String, Object> getTestDataStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test data controller is working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("note", "This endpoint is for testing purposes only");
        return response;
    }

    // Public endpoint to simulate hotel data
    @GetMapping("/hotels/sample")
    public Map<String, Object> getSampleHotelData() {
        Map<String, Object> hotel = new HashMap<>();
        hotel.put("id", 1L);
        hotel.put("name", "Sample Test Hotel");
        hotel.put("email", "test@samplehotel.com");
        hotel.put("phone", "+1234567890");
        hotel.put("address", "123 Test Street");
        hotel.put("district", "Test District");
        hotel.put("locality", "Test Locality");
        hotel.put("description", "A sample hotel for testing purposes");
        hotel.put("isVerified", true);
        return hotel;
    }

    // Public endpoint to simulate room data
    @GetMapping("/rooms/sample")
    public Map<String, Object> getSampleRoomData() {
        Map<String, Object> room = new HashMap<>();
        room.put("id", 1L);
        room.put("roomNumber", "101");
        room.put("roomType", "STANDARD");
        room.put("maxGuests", 2);
        room.put("price", 100.00);
        room.put("isAvailable", true);
        room.put("hotelId", 1L);
        return room;
    }
}
