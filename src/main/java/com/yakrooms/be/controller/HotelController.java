package com.yakrooms.be.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.util.HotelSearchCriteria;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

	@Autowired
    private HotelService hotelService;

    // Create new hotel
    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@RequestBody HotelRequest request) {
        HotelResponse response = hotelService.createHotel(request);
        return ResponseEntity.ok(response);
    }

    // Get hotel by ID
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    // Get all hotels with pagination
    @GetMapping
    public ResponseEntity<Page<HotelResponse>> getAllHotels(Pageable pageable) {
        return ResponseEntity.ok(hotelService.getAllHotels(pageable));
    }

    // Update a hotel by ID
    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long id,
                                                     @RequestBody HotelRequest request) {
        return ResponseEntity.ok(hotelService.updateHotel(id, request));
    }

    // Delete a hotel by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // Verify hotel listing (Admin/Moderator)
    @PostMapping("/{id}/verify")
    public ResponseEntity<Void> verifyHotel(@PathVariable Long id) {
        hotelService.verifyHotel(id);
        return ResponseEntity.ok().build();
    }

    // Search hotels by criteria
    @PostMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(@RequestBody HotelSearchCriteria criteria) {
        return ResponseEntity.ok(hotelService.searchHotels(criteria));
    }
}