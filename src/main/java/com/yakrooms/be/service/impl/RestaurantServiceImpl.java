package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.mapper.RestaurantMapper;
import com.yakrooms.be.dto.request.RestaurantRequest;
import com.yakrooms.be.dto.response.RestaurantResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.Restaurant;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.RestaurantRepository;
import com.yakrooms.be.service.RestaurantService;

import org.springframework.transaction.annotation.Transactional;



@Service
public class RestaurantServiceImpl implements RestaurantService {

	@Autowired
    private RestaurantRepository restaurantRepository;
    
	@Autowired
	private HotelRepository hotelRepository;
    
	private final RestaurantMapper restaurantMapper;
	
	@Autowired
    public RestaurantServiceImpl(RestaurantMapper restaurantMapper) {
		super();
		this.restaurantMapper = restaurantMapper;
	}

	@Override
    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = restaurantMapper.toEntity(request);

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            restaurant.setHotel(hotel);
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(saved);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public RestaurantResponse getRestaurantById(Long id) {
//        Restaurant restaurant = restaurantRepository.findById(id)
//            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
//
//        return restaurantMapper.toDto(restaurant);
//    }
	
	@Override
	@Transactional(readOnly = true)
	public Optional<RestaurantResponse> getRestaurantById(Long id) {
	    return restaurantRepository.findById(id)
	        .map(restaurantMapper::toDto);
	}



    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(restaurantMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurantMapper.updateFromRequest(request, restaurant);

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            restaurant.setHotel(hotel);
        } else {
            restaurant.setHotel(null);
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurantRepository.delete(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByHotel(Long hotelId) {
        return restaurantRepository.findByHotelId(hotelId).stream()
                .map(restaurantMapper::toDto)
                .collect(Collectors.toList());
    }
}