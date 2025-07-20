package com.yakrooms.be.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.request.RestaurantRequest;
import com.yakrooms.be.dto.response.RestaurantResponse;

@Service
public interface RestaurantService {

    RestaurantResponse createRestaurant(RestaurantRequest request);

    Optional<RestaurantResponse> getRestaurantById(Long id);

    List<RestaurantResponse> getAllRestaurants();

    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);

    void deleteRestaurant(Long id);

    List<RestaurantResponse> getRestaurantsByHotel(Long hotelId);

}