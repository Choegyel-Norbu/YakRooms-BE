package com.yakrooms.be.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.mapper.HotelMapper;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.service.MailService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelMapper hotelMapper;
    private final MailService mailService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HotelServiceImpl(HotelRepository hotelRepository, 
                           UserRepository userRepository,
                           HotelMapper hotelMapper, 
                           MailService mailService,
                           JdbcTemplate jdbcTemplate) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.hotelMapper = hotelMapper;
        this.mailService = mailService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public HotelResponse createHotel(HotelRequest request, Long userId) {
        // Validate input
        if (request == null || userId == null) {
            throw new IllegalArgumentException("Hotel request and user ID cannot be null");
        }

        if (hotelRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("Hotel with this email already exists");
        }

        // Find user first to fail fast if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Create and save hotel
        Hotel hotel = hotelMapper.toEntity(request);
        hotel.getUsers().add(user);
        
        // Update user relationship
        user.setHotel(hotel);
        user.addRole(Role.HOTEL_ADMIN);

        Hotel savedHotel = hotelRepository.save(hotel);
        logger.info("Created new hotel with ID: {} for user: {}", savedHotel.getId(), userId);

        return hotelMapper.toDto(savedHotel);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelListingDto getListingForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<Object[]> rawResults = hotelRepository.findRawHotelListingByUserId(userId);

        if (rawResults.isEmpty()) {
            throw new EntityNotFoundException("No hotel found for user ID: " + userId);
        }

        Object[] row = rawResults.get(0);

        return new HotelListingDto(
                ((Number) row[0]).longValue(), // id
                (String) row[1],               // name
                (String) row[2],               // address
                (String) row[3],               // district
                (String) row[4],               // description
                (String) row[5],               // phone
                (Boolean) row[6],              // isVerified
                ((Timestamp) row[7]).toLocalDateTime(), // createdAt
                (String) row[9],               // photoUrls (comma-separated)
                (String) row[10],              // amenities (comma-separated)
                (String) row[8]                // additional field
        );
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
        if (id == null || request == null) {
            throw new IllegalArgumentException("Hotel ID and request cannot be null");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        // Use mapper to update - it handles null checking
        hotelMapper.updateHotelFromRequest(request, hotel);
        
        Hotel savedHotel = hotelRepository.save(hotel);
        logger.info("Updated hotel with ID: {}", id);

        return hotelMapper.toDto(savedHotel);
    }

    @Override
    public void deleteHotel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + id);
        }

        // Delete all bookings associated with this hotel to avoid room constraint
        jdbcTemplate.update("DELETE FROM booking WHERE hotel_id = ?", id);
        logger.info("Deleted all bookings for hotel with ID: {}", id);

        // Find all users associated with this hotel and set their hotel to null
        List<User> usersWithHotel = userRepository.findByHotelId(id);
        for (User user : usersWithHotel) {
            user.setHotel(null);
            // Remove HOTEL_ADMIN role if user has it
            user.removeRole(Role.HOTEL_ADMIN);
        }
        userRepository.saveAll(usersWithHotel);
        
        logger.info("Disassociated {} users from hotel with ID: {}", usersWithHotel.size(), id);

        // Now delete the hotel
        hotelRepository.deleteById(id);
        logger.info("Deleted hotel with ID: {}", id);
    }

    @Override
    public void verifyHotel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        if (hotel.isVerified()) {
            logger.warn("Hotel with ID: {} is already verified", id);
            return; // Already verified, no need to process
        }

        hotel.setVerified(true);
        hotelRepository.save(hotel);

        // Send verification email if hotel has email
        if (StringUtils.hasText(hotel.getEmail())) {
            try {
                // Use hotel's email directly instead of separate query
                mailService.sendHotelVerificationEmail(hotel.getEmail(), hotel.getName());
                logger.info("Verification email sent for hotel: {}", hotel.getName());
            } catch (Exception e) {
                logger.error("Failed to send verification email for hotel ID: {}", id, e);
                // Don't throw exception here as hotel is already verified
            }
        }

        logger.info("Verified hotel with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        return hotelMapper.toDto(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> searchHotels(String district, String hotelType, int page, int size) {
        // Validate pagination parameters
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be > 0");
        }

        Pageable pageable = PageRequest.of(page, size);

        // Normalize empty strings to null
        String cleanDistrict = StringUtils.hasText(district) ? district.trim() : null;
        String cleanHotelType = StringUtils.hasText(hotelType) ? hotelType.trim() : null;

        Page<Hotel> hotelPage = hotelRepository.findByDistrictAndHotelType(cleanDistrict, cleanHotelType, pageable);

        return hotelPage.map(hotelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelWithPriceProjection> getTopThreeHotels() {
        return hotelRepository.findTop3VerifiedHotelsWithPhotosAndPrice();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelWithLowestPriceProjection> getAllHotelsSortedByLowestPrice(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelWithLowestPriceProjection> getAllHotelsSortedByHighestPrice(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceDesc(pageable);
    }
}