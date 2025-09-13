package com.yakrooms.be.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.cache.HotelListingCacheDto;
import com.yakrooms.be.dto.cache.HotelListingPageCacheDto;
import com.yakrooms.be.dto.cache.HotelSearchPageCacheDto;
import com.yakrooms.be.dto.cache.HotelTopCacheDto;
import com.yakrooms.be.dto.mapper.CacheMapper;
import com.yakrooms.be.dto.mapper.HotelMapper;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.request.HotelDeletionRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.exception.ResourceConflictException;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.entity.User;
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

import com.yakrooms.be.service.CacheService;
import com.yakrooms.be.service.HotelService;
import com.yakrooms.be.service.MailService;
import com.yakrooms.be.service.NotificationService;


import jakarta.persistence.EntityNotFoundException;

@Service
@Primary
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
    private final CacheService cacheService;
    private final CacheMapper cacheMapper;
    private final NotificationService notificationService;

    public HotelServiceImpl(HotelRepository hotelRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           RoomRepository roomRepository,
                           NotificationRepository notificationRepository,
                           StaffRepository staffRepository,
                           ReviewRepository reviewRepository,
                           RestaurantRepository restaurantRepository,
                           HotelMapper hotelMapper,
                           MailService mailService,
                           CacheService cacheService,
                           CacheMapper cacheMapper,
                           NotificationService notificationService) {
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
        this.cacheService = cacheService;
        this.cacheMapper = cacheMapper;
        this.notificationService = notificationService;
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
        
        // Evict all hotel-related caches to ensure new hotel appears in listings
        cacheService.evictAllHotelCaches();
        // Also evict user-specific cache for this user
        cacheService.evictUserHotelsFromCache(userId);
        log.info("Evicted all hotel caches after creating hotel with ID: {}", savedHotel.getId());
        
        return hotelMapper.toDto(savedHotel);
    }

    @Override
    public HotelListingDto getListingForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Try to get from cache first
        Optional<HotelListingCacheDto> cached = cacheService.getUserHotelsFromCache(userId);
        if (cached.isPresent()) {
            log.debug("Retrieved user hotels from cache for user ID: {}", userId);
            return convertCacheDtoToHotelListingDto(cached.get());
        }

        log.debug("Fetching hotel listing for user from database: {}", userId);
        HotelListingProjection projection = hotelRepository.findHotelListingByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No hotel found for user ID: " + userId));

        HotelListingDto result = HotelListingDto.fromProjection(projection);
        
        // Convert to cache DTO and store for future requests
        HotelListingCacheDto cacheDto = cacheMapper.toHotelListingCacheDto(projection);
        cacheService.putUserHotelsInCache(userId, cacheDto);

        return result;
    }

    @Override
    public Page<HotelListingPageCacheDto> getAllHotels(Pageable pageable) {
        log.debug("Fetching all hotels from database with pagination: {}", pageable);

        // Try to get from cache first
        Optional<HotelListingPageCacheDto> cached = cacheService.getHotelListingsPageFromCache("all_hotels", pageable.getPageNumber(), pageable.getPageSize());
        if (cached.isPresent()) {
            log.debug("Retrieved all hotels from cache - returning paginated cache DTO");
            // Create a Page containing the single page DTO
            return new org.springframework.data.domain.PageImpl<>(
                List.of(cached.get()),
                org.springframework.data.domain.PageRequest.of(cached.get().getPageNumber(), cached.get().getPageSize()),
                cached.get().getTotalElements()
            );
        }

        // If not in cache, fetch from database and convert to cache DTO
        log.debug("Fetching all hotels from database with pagination: {}", pageable);
        Page<HotelWithLowestPriceProjection> hotels = hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);

        // Convert to cache DTO and store
        HotelListingPageCacheDto cacheDto = cacheMapper.toHotelListingPageCacheDto(hotels, "all_hotels");
        cacheService.putHotelListingsPageInCache("all_hotels", pageable.getPageNumber(), pageable.getPageSize(), cacheDto);

        log.debug("Found {} hotels (converted to cache DTO)", hotels.getTotalElements());
        
        // Return the cache DTO as a paginated response
        return new org.springframework.data.domain.PageImpl<>(
            List.of(cacheDto),
            pageable,
            hotels.getTotalElements()
        );
    }
    
    @Override
    public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable) {
        log.debug("Fetching all hotels for super admin from database with pagination: {}", pageable);
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
        
        // Evict hotel-specific caches and all hotel-related caches
        cacheService.evictHotelDetailsFromCache(id);
        cacheService.evictAllHotelCaches();
        
        // Evict user-specific cache for hotel owners
        if (savedHotel.getUsers() != null) {
            savedHotel.getUsers().forEach(user -> {
                cacheService.evictUserHotelsFromCache(user.getId());
            });
        }
        
        log.info("Evicted all hotel caches after updating hotel with ID: {}", id);
        
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
        
        // Evict DTO-based caches as well
        cacheService.evictHotelDetailsFromCache(id);
        cacheService.evictAllHotelCaches();
        
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
    public Page<HotelSearchPageCacheDto> searchHotels(String district, String locality, String hotelType, int page, int size) {
        log.debug("Searching hotels with district: {}, locality: {}, hotelType: {}, page: {}, size: {}", 
                district, locality, hotelType, page, size);

        validatePagination(page, size);

        // Try to get from cache first
        Optional<HotelSearchPageCacheDto> cached = cacheService.getHotelSearchPageFromCache(district, locality, hotelType, page, size);
        if (cached.isPresent()) {
            log.debug("Retrieved hotel search results from cache - returning cache DTO directly");
            return new PageImpl<>(
                List.of(cached.get()),
                PageRequest.of(cached.get().getPageNumber(), cached.get().getPageSize()),
                cached.get().getTotalElements()
            );
        }

        // If not in cache, fetch from database and convert to cache DTO
        Pageable pageable = PageRequest.of(page, size);
        log.debug("Searching hotels from database with filters - district: {}, locality: {}, type: {}, page: {}, size: {}", 
                 district, locality, hotelType, page, size);
        Page<HotelWithLowestPriceProjection> hotelPage = hotelRepository.findAllVerifiedHotelsWithLowestPriceSortedAndFiltered(district, locality, hotelType, pageable);

        // Convert to cache DTO and store
        HotelSearchPageCacheDto cacheDto = cacheMapper.toHotelSearchPageCacheDto(hotelPage, district, locality, hotelType);
        cacheService.putHotelSearchPageInCache(district, locality, hotelType, page, size, cacheDto);

        log.debug("Found {} hotels for search criteria (converted to cache DTO)", hotelPage.getTotalElements());
        return new PageImpl<>(
            List.of(cacheDto),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }

    @Override
    public List<HotelWithPriceProjection> getTopThreeHotels() {
        log.info("🔍 Checking cache for top three hotels...");

        // Try to get from cache first
        Optional<List<HotelTopCacheDto>> cached = cacheService.getTopHotelsFromCache();
        if (cached.isPresent()) {
            log.info("✅ CACHE HIT: Retrieved top hotels from Redis cache");
            return convertTopCacheDtoListToProjectionList(cached.get());
        }

        log.info("❌ CACHE MISS: Fetching top hotels from database");
        // If not in cache, fetch from database
        List<HotelWithPriceProjection> topHotels = hotelRepository.findTop3VerifiedHotelsWithPhotosAndPrice();

        // Convert to cache DTO and store
        List<HotelTopCacheDto> cacheDtoList = cacheMapper.toHotelTopCacheDtoList(topHotels);
        cacheService.putTopHotelsInCache(cacheDtoList);
        log.info("💾 CACHE STORE: Stored top hotels in Redis cache");

        // Convert to DTOs to avoid JPA proxy serialization issues
        return convertTopCacheDtoListToProjectionList(cacheDtoList);
    }

    @Override
    public Page<HotelListingPageCacheDto> getAllHotelsSortedByLowestPrice(Pageable pageable) {
        log.debug("Fetching hotels sorted by lowest price with pagination: {}", pageable);

        // Try to get from cache first
        Optional<HotelListingPageCacheDto> cached = cacheService.getHotelListingsPageFromCache("lowest_price", pageable.getPageNumber(), pageable.getPageSize());
        if (cached.isPresent()) {
            log.debug("Retrieved hotel listings from cache - returning cache DTO directly");
            return new PageImpl<>(
                List.of(cached.get()),
                PageRequest.of(cached.get().getPageNumber(), cached.get().getPageSize()),
                cached.get().getTotalElements()
            );
        }

        // If not in cache, fetch from database and convert to cache DTO
        log.debug("Fetching hotels sorted by lowest price from database with pagination: {}", pageable);
        Page<HotelWithLowestPriceProjection> hotels = hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);

        // Convert to cache DTO and store
        HotelListingPageCacheDto cacheDto = cacheMapper.toHotelListingPageCacheDto(hotels, "lowest_price");
        cacheService.putHotelListingsPageInCache("lowest_price", pageable.getPageNumber(), pageable.getPageSize(), cacheDto);

        log.debug("Found {} hotels sorted by lowest price (converted to cache DTO)", hotels.getTotalElements());
        return new PageImpl<>(
            List.of(cacheDto),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }

    @Override
    public Page<HotelListingPageCacheDto> getAllHotelsSortedByHighestPrice(Pageable pageable) {
        log.debug("Fetching hotels sorted by highest price with pagination: {}", pageable);

        // Try to get from cache first
        Optional<HotelListingPageCacheDto> cached = cacheService.getHotelListingsPageFromCache("highest_price", pageable.getPageNumber(), pageable.getPageSize());
        if (cached.isPresent()) {
            log.debug("Retrieved hotel listings from cache - returning cache DTO directly");
            return new PageImpl<>(
                List.of(cached.get()),
                PageRequest.of(cached.get().getPageNumber(), cached.get().getPageSize()),
                cached.get().getTotalElements()
            );
        }

        // If not in cache, fetch from database and convert to cache DTO
        log.debug("Fetching hotels sorted by highest price from database with pagination: {}", pageable);
        Page<HotelWithLowestPriceProjection> hotels = hotelRepository.findAllVerifiedHotelsWithLowestPriceDesc(pageable);

        // Convert to cache DTO and store
        HotelListingPageCacheDto cacheDto = cacheMapper.toHotelListingPageCacheDto(hotels, "highest_price");
        cacheService.putHotelListingsPageInCache("highest_price", pageable.getPageNumber(), pageable.getPageSize(), cacheDto);

        log.debug("Found {} hotels sorted by highest price (converted to cache DTO)", hotels.getTotalElements());
        return new PageImpl<>(
            List.of(cacheDto),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
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
    /**
     * Convert HotelTopCacheDto list back to HotelWithPriceProjection list
     * Creates proper projection implementations to avoid JPA proxy serialization issues
     */
    private List<HotelWithPriceProjection> convertTopCacheDtoListToProjectionList(List<HotelTopCacheDto> cacheDtoList) {
        if (cacheDtoList == null || cacheDtoList.isEmpty()) {
            return List.of();
        }

        return cacheDtoList.stream()
            .map(this::convertTopCacheDtoToProjection)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convert HotelTopCacheDto to HotelWithPriceProjection
     * Creates a simple implementation to avoid JPA proxy serialization issues
     */
    private HotelWithPriceProjection convertTopCacheDtoToProjection(HotelTopCacheDto dto) {
        if (dto == null) {
            return null;
        }

        return new HotelWithPriceProjection() {
            @Override
            public Long getId() { return dto.getId(); }
            @Override
            public String getName() { return dto.getName(); }
            @Override
            public String getEmail() { return dto.getEmail(); }
            @Override
            public String getPhone() { return dto.getPhone(); }
            @Override
            public String getAddress() { return dto.getAddress(); }
            @Override
            public String getDistrict() { return dto.getDistrict(); }
            @Override
            public String getLocality() { return dto.getLocality(); }
            @Override
            public String getLogoUrl() { return dto.getLogoUrl(); }
            @Override
            public String getDescription() { return dto.getDescription(); }
            @Override
            public Boolean getIsVerified() { return dto.isVerified(); }
            @Override
            public String getWebsiteUrl() { return dto.getWebsiteUrl(); }
            @Override
            public java.time.LocalDateTime getCreatedAt() { return dto.getCreatedAt(); }
            @Override
            public String getLicenseUrl() { return dto.getLicenseUrl(); }
            @Override
            public String getIdProofUrl() { return dto.getIdProofUrl(); }
            @Override
            public Double getLowestPrice() { return dto.getLowestPrice(); }
            @Override
            public String getPhotoUrls() { return dto.getPhotoUrls(); }
            @Override
            public String getPhotoUrl() { return dto.getPhotoUrl(); }
            @Override
            public Double getAvgRating() { return dto.getAvgRating(); }
        };
    }

    /**
     * Convert HotelListingCacheDto to HotelListingDto
     * Helper method for cache-to-service DTO conversion
     */
    private HotelListingDto convertCacheDtoToHotelListingDto(HotelListingCacheDto cacheDto) {
        return new HotelListingDto(
            cacheDto.getId(),
            cacheDto.getName(),
            cacheDto.getAddress(),
            cacheDto.getDistrict(),
            cacheDto.getLocality(),
            cacheDto.getDescription(),
            cacheDto.getPhone(),
            cacheDto.isVerified(),
            cacheDto.getCreatedAt(),
            cacheDto.getPhotoUrls(),
            cacheDto.getAmenities(),
            cacheDto.getHotelType() != null ? cacheDto.getHotelType().toString() : null,
            null, // checkinTime - not available in cache DTO
            null  // checkoutTime - not available in cache DTO
        );
    }

    @Override
    @Transactional
    public Map<String, Object> requestHotelDeletion(HotelDeletionRequest request) {
        log.info("Processing hotel deletion request for hotel ID: {}", request.getHotelId());
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find the hotel
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + request.getHotelId()));
            
            // Check if deletion is already requested
            if (hotel.isDeletionRequested()) {
                result.put("success", false);
                result.put("message", "Hotel deletion request already exists");
                result.put("alreadyRequested", true);
                return result;
            }
            
            // Update hotel with deletion request details
            hotel.setDeletionRequested(true);
            hotel.setDeletionReason(request.getDeletionReason());
            hotel.setDeletionRequestedAt(java.time.LocalDateTime.now());
            
            hotelRepository.save(hotel);
            log.info("Updated hotel {} with deletion request", hotel.getId());
            
            // Find hotel owner for notification
            User hotelOwner = userRepository.findByHotelIdAndRole(hotel.getId(), Role.HOTEL_ADMIN)
                    .orElse(null);
            
                com.yakrooms.be.model.entity.Notification notification = new com.yakrooms.be.model.entity.Notification();
                notification.setUser(hotelOwner);
                notification.setBooking(null); // No booking associated with hotel deletion
                notification.setTitle("Hotel Deletion Request");
                notification.setMessage("Rquest for hotel deletion");
                notification.setType(com.yakrooms.be.model.enums.NotificationType.HOTEL_DELETION_REQUEST.name());
                notification.setRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                
                notificationRepository.save(notification);
            
            result.put("success", true);
            result.put("message", "Hotel deletion request submitted successfully");
            result.put("hotelId", hotel.getId());
            result.put("hotelName", hotel.getName());
            result.put("emailSent", null);
            result.put("adminsNotified",null);
            
        } catch (Exception e) {
            log.error("Error processing hotel deletion request: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Failed to process hotel deletion request: " + e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Page<HotelResponse> getHotelsWithDeletionRequests(Pageable pageable) {
        log.debug("Fetching hotels with deletion requests with pagination: {}", pageable);
        
        Page<Hotel> hotels = hotelRepository.findByDeletionRequestedTrue(pageable);
        
        return hotels.map(hotelMapper::toDto);
    }

}