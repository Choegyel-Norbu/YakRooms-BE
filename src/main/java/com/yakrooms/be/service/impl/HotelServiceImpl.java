package com.yakrooms.be.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import com.yakrooms.be.repository.NotificationRepository;
import com.yakrooms.be.repository.RestaurantRepository;
import com.yakrooms.be.repository.ReviewRepository;
import com.yakrooms.be.repository.RoomRepository;
import com.yakrooms.be.repository.StaffRepository;
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
    private final RoomRepository roomRepository;
    private final NotificationRepository notificationRepository;
    private final StaffRepository staffRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final HotelMapper hotelMapper;
    private final MailService mailService;


    @Autowired
    public HotelServiceImpl(HotelRepository hotelRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           RoomRepository roomRepository,
                           NotificationRepository notificationRepository,
                           StaffRepository staffRepository,
                           ReviewRepository reviewRepository,
                           RestaurantRepository restaurantRepository,
                           HotelMapper hotelMapper,
                           MailService mailService) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.notificationRepository = notificationRepository;
        this.staffRepository = staffRepository;
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.hotelMapper = hotelMapper;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"hotelListings", "searchResults", "topHotels"}, allEntries = true)
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
    @Cacheable(value = "userHotels", key = "#userId")
    public HotelListingDto getListingForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        log.debug("Fetching hotel listing for user from database: {}", userId);
        HotelListingProjection projection = hotelRepository.findHotelListingByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No hotel found for user ID: " + userId));

        return HotelListingDto.fromProjection(projection);
    }

    @Override
    @Cacheable(value = "hotelListings", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<HotelWithLowestPriceProjection> getAllHotels(Pageable pageable) {
        log.debug("Fetching all hotels from database with pagination: {}", pageable);
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
    }
    
    @Override
    public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable) {
        log.debug("Fetching all hotels for super admin from database with pagination: {}", pageable);
        return hotelRepository.findAll(pageable).map(hotelMapper::toDto);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "hotelDetails", key = "#id"),
        @CacheEvict(value = {"hotelListings", "searchResults", "topHotels"}, allEntries = true)
    })
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

        // Delete notifications first (they reference room_id)
        notificationRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted notifications for hotel ID: {}", id);

        // Delete bookings in batch
        bookingRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted bookings for hotel ID: {}", id);

        // Delete rooms in batch (this will cascade to room items and other room-related entities)
        roomRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted rooms for hotel ID: {}", id);

        // Delete staff in batch
        staffRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted staff for hotel ID: {}", id);

        // Delete reviews in batch
        reviewRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted reviews for hotel ID: {}", id);

        // Delete restaurant in batch
        restaurantRepository.deleteByHotelIdInBatch(id);
        log.info("Deleted restaurant for hotel ID: {}", id);

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
    public Map<String, Object> verifyHotel(Long id) {
        Map<String, Object> result = new HashMap<>();
        
        if (id == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        // Check if already verified to avoid unnecessary DB write
        if (hotelRepository.isHotelVerified(id)) {
            log.info("Hotel with ID: {} is already verified", id);
            
            Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
            
            result.put("hotelVerified", true);
            result.put("hotelId", id);
            result.put("hotelName", hotel.getName());
            result.put("alreadyVerified", true);
            result.put("emailSent", false);
            result.put("emailError", "Hotel was already verified, no email sent");
            
            return result;
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        hotel.setVerified(true);
        hotelRepository.save(hotel);
        log.info("Verified hotel with ID: {}", id);
        
        result.put("hotelVerified", true);
        result.put("hotelId", id);
        result.put("hotelName", hotel.getName());
        result.put("alreadyVerified", false);
        
        // Try to send email synchronously for immediate feedback
        if (StringUtils.hasText(hotel.getEmail())) {
            try {
                mailService.sendHotelVerificationEmail(hotel.getEmail(), hotel.getName());
                log.info("Verification email sent for hotel: {}", hotel.getName());
                result.put("emailSent", true);
                result.put("emailAddress", hotel.getEmail());
            } catch (Exception e) {
                log.error("Failed to send verification email for hotel: {}", hotel.getName(), e);
                result.put("emailSent", false);
                result.put("emailError", e.getMessage());
                result.put("emailAddress", hotel.getEmail());
            }
        } else {
            result.put("emailSent", false);
            result.put("emailError", "No email address provided for hotel");
        }
        
        return result;
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
    @Cacheable(value = "hotelDetails", key = "#hotelId")
    public HotelResponse getHotelById(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        log.debug("Fetching hotel details from database for ID: {}", hotelId);
        HotelWithCollectionsAndRatingProjection hotelProjection = hotelRepository.findByIdWithCollections(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        return hotelMapper.toDto(hotelProjection);
    }
    
    @Override
    @Cacheable(value = "searchResults", key = "'search_' + #district + '_' + #locality + '_' + #hotelType + '_' + #page + '_' + #size")
    public Page<HotelWithLowestPriceProjection> searchHotels(String district, String locality, String hotelType, int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        log.debug("Searching hotels from database with filters - district: {}, locality: {}, type: {}, page: {}, size: {}", 
                 district, locality, hotelType, page, size);
        Page<HotelWithLowestPriceProjection> hotelPage = hotelRepository.findAllVerifiedHotelsWithLowestPriceSortedAndFiltered(district, locality, hotelType, pageable);
        return hotelPage;
    }

    @Override
    @Cacheable(value = "topHotels", key = "'top3'")
    public List<HotelWithPriceProjection> getTopThreeHotels() {
        log.debug("Fetching top three hotels from database");
        return hotelRepository.findTop3VerifiedHotelsWithPhotosAndPrice();
    }

    @Override
    @Cacheable(value = "hotelListings", key = "'lowest_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<HotelWithLowestPriceProjection> getAllHotelsSortedByLowestPrice(Pageable pageable) {
        log.debug("Fetching hotels sorted by lowest price from database with pagination: {}", pageable);
        return hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
    }

    @Override
    @Cacheable(value = "hotelListings", key = "'highest_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<HotelWithLowestPriceProjection> getAllHotelsSortedByHighestPrice(Pageable pageable) {
        log.debug("Fetching hotels sorted by highest price from database with pagination: {}", pageable);
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