package com.yakrooms.be.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.mapper.HotelMapper;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.HotelType;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.service.MailService;
import com.yakrooms.be.util.HotelSearchCriteria;

import jakarta.persistence.EntityNotFoundException;

@Service
public class HotelServiceImpl implements HotelService {

	@Autowired
	HotelRepository hotelRepository;

	@Autowired
	UserRepository userRepository;

	private final HotelMapper hotelMapper;

	@Autowired
	private MailService mailService;

	@Autowired
	public HotelServiceImpl(HotelMapper hotelMapper) {
		this.hotelMapper = hotelMapper;
	}

	@Override
	public HotelResponse createHotel(HotelRequest request, Long userId) {
		if (hotelRepository.existsByEmail(request.getEmail())) {
			throw new ResourceConflictException("There was problem submitting listing");
		}

		Hotel hotel = hotelMapper.toEntity(request);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		user.setHotel(hotel);
		user.setRole(Role.HOTEL_ADMIN);

		hotel.getUsers().add(user);

		return hotelMapper.toDto(hotelRepository.save(hotel));
	}

	@Override
	@Transactional(readOnly = true)
	public HotelListingDto getListingForUser(Long userId) {
		List<Object[]> rawResults = hotelRepository.findRawHotelListingByUserId(userId);

		if (rawResults.isEmpty()) {
			throw new EntityNotFoundException("No hotel found for user ID: " + userId);
		}

		Object[] row = rawResults.get(0);

		return new HotelListingDto(((Number) row[0]).longValue(), // id
				(String) row[1], // name
				(String) row[2], // address
				(String) row[3], // district
				(String) row[4], // description
				(String) row[5], // phone
				(Boolean) row[6], // isVerified
				((Timestamp) row[7]).toLocalDateTime(), // createdAt
				(String) row[9], // photoUrls (comma-separated)
				(String) row[10], // amenities (comma-separated),
				(String) row[8]);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<HotelWithLowestPriceProjection> getAllHotels(Pageable pageable) {
		return hotelRepository.findAllVerifiedHotelsWithLowestPrice(pageable);

	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable) {
		return hotelRepository.findAll(pageable).map(hotelMapper::toDto);
	}

	@Override
	public HotelResponse updateHotel(Long id, HotelRequest request) {
		Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

		hotelMapper.updateHotelFromRequest(request, hotel);
		hotel.setAddress(request.getAddress());
		hotel.setAmenities(request.getAmenities());
		hotel.setDescription(request.getDescription());
		hotel.setName(request.getName());
		hotel.setPhone(request.getPhone());

		hotelRepository.save(hotel);

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

		if (hotel.getEmail() != null && !hotel.getEmail().isBlank()) {
			String email = hotelRepository.findOwnerEmailByHotelId(id);
			System.out.println("Email: " + email);
			mailService.sendHotelVerificationEmail(email, hotel.getName());
		}

	}

	

	@Override
	@Transactional(readOnly = true)
	public HotelResponse getHotelById(Long hotelId) {
		Hotel hotel = hotelRepository.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

		return hotelMapper.toDto(hotel);
	}

	@Override
	public Page<HotelResponse> searchHotels(String district, String hotelType, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		// Normalize empty string to null
		String cleanDistrict = (district != null && !district.trim().isEmpty()) ? district : null;
		String cleanHotelType = (hotelType != null && !hotelType.trim().isEmpty()) ? hotelType : null;

		Page<Hotel> hotelPage = hotelRepository.findByDistrictAndHotelType(cleanDistrict, cleanHotelType, pageable);

		return hotelPage.map(hotelMapper::toDto);
	}

	@Override
	public List<HotelWithPriceProjection> getTopThreeHotels() {
		List<HotelWithPriceProjection> hotels = hotelRepository.findTop3VerifiedHotelsWithPhotosAndPrice();

		return hotels;
	}

    @Override
    @Transactional(readOnly = true)
    public Page<com.yakrooms.be.projection.HotelWithLowestPriceProjection> getAllHotelsSortedByLowestPrice(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<com.yakrooms.be.projection.HotelWithLowestPriceProjection> getAllHotelsSortedByHighestPrice(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceDesc(pageable);
    }

}
