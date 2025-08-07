package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
import com.yakrooms.be.model.enums.HotelType;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithCollectionsAndRatingProjection;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.repository.BookingRepository;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.service.MailService;

import jakarta.persistence.EntityNotFoundException;
@Service
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final HotelMapper hotelMapper;
    private final MailService mailService;

    @Autowired
    public HotelServiceImpl(HotelRepository hotelRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           HotelMapper hotelMapper,
                           MailService mailService) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.hotelMapper = hotelMapper;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public HotelResponse createHotel(HotelRequest request, Long userId) {
        validateCreateHotelRequest(request, userId);

        // Check email uniqueness
        if (hotelRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ResourceConflictException("Hotel with email " + request.getEmail() + " already exists");
        }

        // Fetch user with roles to avoid lazy loading
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Create hotel
        Hotel hotel = hotelMapper.toEntity(request);
        hotel.addUser(user);
        
        // Update user
        user.addRole(Role.HOTEL_ADMIN);

        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Created hotel with ID: {} for user: {}", savedHotel.getId(), userId);

        return hotelMapper.toDto(savedHotel);
    }

    @Override
    public HotelListingDto getListingForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        HotelListingProjection projection = hotelRepository.findHotelListingByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No hotel found for user ID: " + userId));

        return HotelListingDto.fromProjection(projection);
    }

    @Override
    public Page<HotelWithLowestPriceProjection> getAllHotels(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
    }
    
    @Override
    public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable) {
        return hotelRepository.findAll(pageable).map(hotelMapper::toDto);
    }

    @Override
    @Transactional
    public HotelResponse updateHotel(Long id, HotelRequest request) {
        validateUpdateHotelRequest(id, request);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null && !hotel.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            if (hotelRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
                throw new ResourceConflictException("Hotel with email " + request.getEmail() + " already exists");
            }
        }


        hotelMapper.updateHotelFromRequest(request, hotel);
        
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Updated hotel with ID: {}", id);

        return hotelMapper.toDto(savedHotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        // Delete bookings in batch
        bookingRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted bookings for hotel ID: {}", id);

        // Update users in batch
        List<User> users = userRepository.findByHotelIdWithRoles(id);
        users.forEach(user -> {
            user.setHotel(null);
            user.removeRole(Role.HOTEL_ADMIN);
        });
        userRepository.saveAll(users);
        log.info("Updated {} users for hotel ID: {}", users.size(), id);

        // Delete hotel
        hotelRepository.delete(hotel);
        log.info("Deleted hotel with ID: {}", id);
    }

    @Override
    @Transactional
    public void verifyHotel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        // Check if already verified to avoid unnecessary DB write
        if (hotelRepository.isHotelVerified(id)) {
            log.info("Hotel with ID: {} is already verified", id);
            return;
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        hotel.setVerified(true);
        hotelRepository.save(hotel);
        log.info("Verified hotel with ID: {}", id);

        // Send email asynchronously
        sendVerificationEmailAsync(hotel);
    }

    @Async
    protected void sendVerificationEmailAsync(Hotel hotel) {
        if (StringUtils.hasText(hotel.getEmail())) {
            try {
                mailService.sendHotelVerificationEmail(hotel.getEmail(), hotel.getName());
                log.info("Verification email sent for hotel: {}", hotel.getName());
            } catch (Exception e) {
                log.error("Failed to send verification email for hotel: {}", hotel.getName(), e);
            }
        }
    }

    @Override
    public HotelResponse getHotelById(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        HotelWithCollectionsAndRatingProjection hotelProjection = hotelRepository.findByIdWithCollections(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        return hotelMapper.toDto(hotelProjection);
    }

    @Override
    public Page<HotelResponse> searchHotels(String district, String hotelType, int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);
        
        String normalizedDistrict = normalizeSearchParam(district);
        HotelType type = parseHotelType(hotelType);

        Page<Hotel> hotelPage = hotelRepository.findVerifiedHotelsByFilters(normalizedDistrict, type, pageable);
        return hotelPage.map(hotelMapper::toDto);
    }

    @Override
    public List<HotelWithPriceProjection> getTopThreeHotels() {
        return hotelRepository.findTop3VerifiedHotelsWithPhotosAndPrice();
    }

    @Override
    public Page<HotelWithLowestPriceProjection> getAllHotelsSortedByLowestPrice(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
    }

    @Override
    public Page<HotelWithLowestPriceProjection> getAllHotelsSortedByHighestPrice(Pageable pageable) {
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceDesc(pageable);
    }

    // Validation methods
    private void validateCreateHotelRequest(HotelRequest request, Long userId) {
        if (request == null) {
            throw new IllegalArgumentException("Hotel request cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException("Hotel email is required");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("Hotel name is required");
        }
    }

    private void validateUpdateHotelRequest(Long id, HotelRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Hotel request cannot be null");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }

    private String normalizeSearchParam(String param) {
        return StringUtils.hasText(param) ? param.trim() : null;
    }

    private HotelType parseHotelType(String hotelType) {
        if (!StringUtils.hasText(hotelType)) {
            return null;
        }
        try {
            return HotelType.valueOf(hotelType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid hotel type: {}", hotelType);
            return null;
        }
    }
}