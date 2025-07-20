package com.yakrooms.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yakrooms.be.dto.request.RestaurantRequest;
import com.yakrooms.be.dto.response.RestaurantResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.service.RestaurantService;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

	@Autowired
    private RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.createRestaurant(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
    	 RestaurantResponse response = restaurantService.getRestaurantById(id)
    		        .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
    		    
    		    return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantsByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByHotel(hotelId));
    }
}