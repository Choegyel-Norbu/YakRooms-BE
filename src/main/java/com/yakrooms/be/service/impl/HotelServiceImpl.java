package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.mapper.HotelMapper;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.util.HotelSearchCriteria;

public class HotelServiceImpl implements HotelService {

	@Autowired
	HotelRepository hotelRepository;

	private HotelMapper hotelMapper;

	@Override
	public HotelResponse createHotel(HotelRequest request) {
		if (hotelRepository.existsByEmail(request.getEmail())) {
			throw new ResourceConflictException("Email already registered");
		}

		Hotel hotel = hotelMapper.toEntity(request);

//        if (request.getLogo() != null) {
//            String logoUrl = fileStorageService.storeFile(request.getLogo());
//            hotel.setLogoUrl(logoUrl);
//        }
//        
		return hotelMapper.toDto(hotelRepository.save(hotel));
	}

	@Override
	@Transactional(readOnly = true)

	public HotelResponse getHotelById(Long id) {
		return hotelRepository.findById(id).map(hotelMapper::toDto)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<HotelResponse> getAllHotels(Pageable pageable) {
		return hotelRepository.findAll(pageable).map(hotelMapper::toDto);
	}

	@Override
	public HotelResponse updateHotel(Long id, HotelRequest request) {
		Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

		hotelMapper.updateHotelFromRequest(request, hotel);

//        if (request.getLogo() != null) {
//            String newLogoUrl = fileStorageService.storeFile(request.getLogo());
//            fileStorageService.deleteFile(hotel.getLogoUrl()); // Delete old logo
//            hotel.setLogoUrl(newLogoUrl);
//        }

		return hotelMapper.toDto(hotel);
	}

	@Override
	public void deleteHotel(Long id) {
		Hotel hotel = hotelRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

		hotelRepository.delete(hotel);

	}

	@Override
	public void verifyHotel(Long id) {
		Hotel hotel = hotelRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

	    hotel.setVerified(true); 
	    hotelRepository.save(hotel);

	}

	@Override
	public List<HotelResponse> searchHotels(HotelSearchCriteria criteria) {
	    Specification<Hotel> spec = Specification.where(null);

	    if (criteria.getLocation() != null) {
	        spec = spec.and((root, query, cb) ->
	            cb.like(cb.lower(root.get("location")), "%" + criteria.getLocation().toLowerCase() + "%"));
	    }

	    if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
	        spec = spec.and((root, query, cb) ->
	            cb.between(root.get("pricePerNight"), criteria.getMinPrice(), criteria.getMaxPrice()));
	    }

	    if (criteria.getMinRating() != null) {
	        spec = spec.and((root, query, cb) ->
	            cb.greaterThanOrEqualTo(root.get("rating"), criteria.getMinRating()));
	    }

	    if (criteria.getKeyword() != null) {
	        spec = spec.and((root, query, cb) ->
	            cb.or(
	                cb.like(cb.lower(root.get("name")), "%" + criteria.getKeyword().toLowerCase() + "%"),
	                cb.like(cb.lower(root.get("description")), "%" + criteria.getKeyword().toLowerCase() + "%")
	            )
	        );
	    }

	    List<Hotel> hotels = hotelRepository.findAll(spec);
	    return hotels.stream()
	            .map(hotelMapper::toDto)
	            .collect(Collectors.toList());
	}


}
