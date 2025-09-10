package com.yakrooms.be.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.cache.*;
import com.yakrooms.be.dto.mapper.CacheMapper;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.exception.ResourceNotFoundException;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithCollectionsAndRatingProjection;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.repository.HotelRepository;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.CacheService;
import com.yakrooms.be.service.HotelService;

/**
 * HotelService implementation with DTO-based caching
 * Uses CacheService to manage Redis operations with clean DTOs instead of JPA entities
 */
@Service("hotelServiceCacheImpl")
public class HotelServiceCacheImpl implements HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelServiceCacheImpl.class);

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheMapper cacheMapper;

    @Autowired
    private CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
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

        // If not in cache, fetch from database
        log.debug("Fetching user hotels from database for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getHotel() == null) {
            throw new ResourceNotFoundException("User does not have an associated hotel");
        }

        HotelListingProjection projection = hotelRepository.findHotelListingByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel listing not found"));

        HotelListingCacheDto cacheDto = cacheMapper.toHotelListingCacheDto(projection);
        HotelListingDto result = convertCacheDtoToHotelListingDto(cacheDto);

        // Store in cache for future requests
        cacheService.putUserHotelsInCache(userId, cacheDto);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel ID cannot be null");
        }

        // Try to get from cache first
        Optional<HotelDetailsCacheDto> cached = cacheService.getHotelDetailsFromCache(hotelId);
        if (cached.isPresent()) {
            log.debug("Retrieved hotel details from cache for ID: {}", hotelId);
            return convertCacheDtoToHotelResponse(cached.get());
        }

        // If not in cache, fetch from database
        log.debug("Fetching hotel details from database for ID: {}", hotelId);
        HotelWithCollectionsAndRatingProjection hotelProjection = hotelRepository.findByIdWithCollections(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        HotelDetailsCacheDto cacheDto = cacheMapper.toHotelDetailsCacheDto(hotelProjection);
        HotelResponse result = convertCacheDtoToHotelResponse(cacheDto);

        // Store in cache for future requests
        cacheService.putHotelDetailsInCache(hotelId, cacheDto);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelSearchPageCacheDto> searchHotels(String district, String locality, String hotelType, int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);
        
        // Fetch from database (no caching in this implementation)
        log.debug("Searching hotels from database with filters - district: {}, locality: {}, type: {}, page: {}, size: {}", 
                 district, locality, hotelType, page, size);
        Page<HotelWithLowestPriceProjection> hotelPage = hotelRepository.findAllVerifiedHotelsWithLowestPriceSortedAndFiltered(district, locality, hotelType, pageable);
        
        // Convert to cache DTO
        HotelSearchPageCacheDto cacheDto = cacheMapper.toHotelSearchPageCacheDto(hotelPage, district, locality, hotelType);
        return new PageImpl<>(
            List.of(cacheDto),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelWithPriceProjection> getTopThreeHotels() {
        log.debug("Fetching top three hotels from database");

        // Try to get from cache first
        Optional<List<HotelTopCacheDto>> cached = cacheService.getTopHotelsFromCache();
        if (cached.isPresent()) {
            log.debug("Retrieved top hotels from cache");
            return convertTopCacheDtoListToProjectionList(cached.get());
        }

        // If not in cache, fetch from database
        List<HotelWithPriceProjection> topHotels = hotelRepository.findTop3VerifiedHotelsWithPhotosAndPrice();
        
        // Convert to cache DTO and store
        List<HotelTopCacheDto> cacheDtoList = cacheMapper.toHotelTopCacheDtoList(topHotels);
        cacheService.putTopHotelsInCache(cacheDtoList);

        return topHotels;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelListingPageCacheDto> getAllHotelsSortedByLowestPrice(Pageable pageable) {
        log.debug("Fetching hotels sorted by lowest price from database with pagination: {}", pageable);
        Page<HotelWithLowestPriceProjection> hotels = hotelRepository.findAllVerifiedHotelsWithLowestPriceSorted(pageable);
        
        // Convert to cache DTO
        HotelListingPageCacheDto cacheDto = cacheMapper.toHotelListingPageCacheDto(hotels, "lowest_price");
        return new PageImpl<>(
            List.of(cacheDto),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelListingPageCacheDto> getAllHotelsSortedByHighestPrice(Pageable pageable) {
        log.debug("Fetching hotels sorted by highest price from database with pagination: {}", pageable);
        Page<HotelWithLowestPriceProjection> hotels = hotelRepository.findAllVerifiedHotelsWithLowestPriceDesc(pageable);
        
        // Convert to cache DTO
        HotelListingPageCacheDto cacheDto = cacheMapper.toHotelListingPageCacheDto(hotels, "highest_price");
        return new PageImpl<>(
            List.of(cacheDto),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }

    @Override
    @Transactional
    public HotelResponse createHotel(HotelRequest hotelRequest, Long userId) {
        // For creation operations, we need to delegate to the original service
        // Then evict relevant caches to ensure consistency
        log.info("Creating hotel and evicting caches for user: {}", userId);
        
        // Since this is a cache-aware service, we need to delegate to the original implementation
        // This would typically be done via composition with the original service
        throw new UnsupportedOperationException("Hotel creation should be handled by the primary service. Use HotelServiceImpl directly.");
    }

    @Override
    @Transactional
    public HotelResponse updateHotel(Long id, HotelRequest hotelRequest) {
        log.info("Updating hotel and evicting caches for hotel ID: {}", id);
        
        // Evict specific hotel cache
        cacheService.evictHotelDetailsFromCache(id);
        // Evict all related caches as hotel data might affect listings and search results
        cacheService.evictAllHotelCaches();
        
        throw new UnsupportedOperationException("Hotel updates should be handled by the primary service. Use HotelServiceImpl directly.");
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        log.info("Deleting hotel and evicting caches for hotel ID: {}", id);
        
        cacheService.evictHotelDetailsFromCache(id);
        cacheService.evictAllHotelCaches();
        
        throw new UnsupportedOperationException("Hotel deletion should be handled by the primary service. Use HotelServiceImpl directly.");
    }

    @Override
    @Transactional
    public Map<String, Object> verifyHotel(Long id) {
        log.info("Verifying hotel and evicting caches for hotel ID: {}", id);
        
        cacheService.evictHotelDetailsFromCache(id);
        cacheService.evictAllHotelCaches();
        
        throw new UnsupportedOperationException("Hotel verification should be handled by the primary service. Use HotelServiceImpl directly.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable) {
        // Admin operations typically don't need caching as they're infrequent
        log.debug("Fetching all hotels for super admin - bypassing cache");
        return hotelRepository.findAll(pageable).map(hotel -> {
            // Convert hotel entity to HotelResponse
            HotelResponse response = new HotelResponse();
            response.setId(hotel.getId());
            response.setName(hotel.getName());
            response.setEmail(hotel.getEmail());
            response.setPhone(hotel.getPhone());
            response.setAddress(hotel.getAddress());
            response.setDistrict(hotel.getDistrict());
            response.setLocality(hotel.getLocality());
            response.setLogoUrl(hotel.getLogoUrl());
            response.setDescription(hotel.getDescription());
            response.setVerified(hotel.isVerified());
            response.setWebsiteUrl(hotel.getWebsiteUrl());
            response.setCreatedAt(hotel.getCreatedAt());
            response.setLicenseUrl(hotel.getLicenseUrl());
            response.setIdProofUrl(hotel.getIdProofUrl());
            response.setHotelType(hotel.getHotelType());
            // Handle collections safely
            if (hotel.getPhotoUrls() != null) {
                response.setPhotoUrls(new java.util.ArrayList<>(hotel.getPhotoUrls()));
            }
            if (hotel.getAmenities() != null) {
                response.setAmenities(new java.util.ArrayList<>(hotel.getAmenities()));
            }
            return response;
        });
    }

    // Helper methods for converting between cache DTOs and service DTOs
    private HotelListingDto convertCacheDtoToHotelListingDto(HotelListingCacheDto cacheDto) {
        // Convert cache DTO to service DTO
        // This is a simplified conversion - you might need more complex logic
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

    private HotelResponse convertCacheDtoToHotelResponse(HotelDetailsCacheDto cacheDto) {
        HotelResponse response = new HotelResponse();
        response.setId(cacheDto.getId());
        response.setName(cacheDto.getName());
        response.setEmail(cacheDto.getEmail());
        response.setPhone(cacheDto.getPhone());
        response.setAddress(cacheDto.getAddress());
        response.setDistrict(cacheDto.getDistrict());
        response.setLocality(cacheDto.getLocality());
        response.setLogoUrl(cacheDto.getLogoUrl());
        response.setDescription(cacheDto.getDescription());
        response.setVerified(cacheDto.isVerified());
        response.setWebsiteUrl(cacheDto.getWebsiteUrl());
        response.setCreatedAt(cacheDto.getCreatedAt());
        response.setLicenseUrl(cacheDto.getLicenseUrl());
        response.setIdProofUrl(cacheDto.getIdProofUrl());
        response.setHotelType(cacheDto.getHotelType());
        if (cacheDto.getPhotoUrls() != null) {
            response.setPhotoUrls(new java.util.ArrayList<>(cacheDto.getPhotoUrls()));
        }
        if (cacheDto.getAmenities() != null) {
            response.setAmenities(new java.util.ArrayList<>(cacheDto.getAmenities()));
        }
        return response;
    }

    private Page<HotelWithLowestPriceProjection> convertPageCacheDtoToPage(PageCacheDto<HotelListingCacheDto> cacheDto, Pageable pageable) {
        if (cacheDto == null || cacheDto.getContent() == null) {
            return Page.empty(pageable);
        }
        
        // Convert cache DTOs to projections
        List<HotelWithLowestPriceProjection> projections = cacheDto.getContent().stream()
                .map(this::convertListingCacheDtoToProjection)
                .collect(Collectors.toList());
        
        return new PageImpl<>(projections, pageable, cacheDto.getTotalElements());
    }

    private Page<HotelWithLowestPriceProjection> convertSearchPageCacheDtoToPage(PageCacheDto<HotelSearchCacheDto> cacheDto, Pageable pageable) {
        if (cacheDto == null || cacheDto.getContent() == null) {
            return Page.empty(pageable);
        }
        
        // Convert search cache DTOs to projections
        List<HotelWithLowestPriceProjection> projections = cacheDto.getContent().stream()
                .map(this::convertSearchCacheDtoToProjection)
                .collect(Collectors.toList());
        
        return new PageImpl<>(projections, pageable, cacheDto.getTotalElements());
    }

    private List<HotelWithPriceProjection> convertTopCacheDtoListToProjectionList(List<HotelTopCacheDto> cacheDtoList) {
        if (cacheDtoList == null) {
            return List.of();
        }
        
        return cacheDtoList.stream()
                .map(this::convertTopCacheDtoToProjection)
                .collect(Collectors.toList());
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }

    // Helper methods for converting cache DTOs to projections
    private HotelWithLowestPriceProjection convertListingCacheDtoToProjection(HotelListingCacheDto cacheDto) {
        return new com.yakrooms.be.projection.impl.HotelWithLowestPriceProjectionImpl(
            cacheDto.getId(),
            cacheDto.getName(),
            null, // email not in listing cache
            cacheDto.getPhone(),
            cacheDto.getAddress(),
            cacheDto.getDistrict(),
            cacheDto.getLocality(),
            null, // logo not in listing cache
            cacheDto.getDescription(),
            cacheDto.isVerified(),
            null, // website not in listing cache
            cacheDto.getCreatedAt(),
            cacheDto.getPhotoUrls(),
            null, // license not in listing cache
            null, // idProof not in listing cache
            cacheDto.getAmenities(),
            cacheDto.getHotelType(),
            cacheDto.getLowestPrice(),
            cacheDto.getPhotoUrls() != null && !cacheDto.getPhotoUrls().isEmpty() ? cacheDto.getPhotoUrls().get(0) : null,
            cacheDto.getAverageRating()
        );
    }
    
    private HotelWithLowestPriceProjection convertSearchCacheDtoToProjection(HotelSearchCacheDto cacheDto) {
        return new com.yakrooms.be.projection.impl.HotelWithLowestPriceProjectionImpl(
            cacheDto.getId(),
            cacheDto.getName(),
            null, // email not in search cache
            cacheDto.getPhone(),
            cacheDto.getAddress(),
            cacheDto.getDistrict(),
            cacheDto.getLocality(),
            null, // logo not in search cache
            cacheDto.getDescription(),
            cacheDto.isVerified(),
            null, // website not in search cache
            cacheDto.getCreatedAt(),
            cacheDto.getPhotoUrls(),
            null, // license not in search cache
            null, // idProof not in search cache
            cacheDto.getAmenities(),
            cacheDto.getHotelType(),
            cacheDto.getLowestPrice(),
            cacheDto.getPhotoUrls() != null && !cacheDto.getPhotoUrls().isEmpty() ? cacheDto.getPhotoUrls().get(0) : null,
            cacheDto.getAverageRating()
        );
    }
    
    private HotelWithPriceProjection convertTopCacheDtoToProjection(HotelTopCacheDto cacheDto) {
        return new HotelWithPriceProjection() {
            @Override
            public Long getId() { return cacheDto.getId(); }
            
            @Override
            public String getName() { return cacheDto.getName(); }
            
            @Override
            public String getEmail() { return cacheDto.getEmail(); }
            
            @Override
            public String getPhone() { return cacheDto.getPhone(); }
            
            @Override
            public String getAddress() { return cacheDto.getAddress(); }
            
            @Override
            public String getDistrict() { return cacheDto.getDistrict(); }
            
            @Override
            public String getLocality() { return cacheDto.getLocality(); }
            
            @Override
            public String getLogoUrl() { return cacheDto.getLogoUrl(); }
            
            @Override
            public String getDescription() { return cacheDto.getDescription(); }
            
            @Override
            public Boolean getIsVerified() { return cacheDto.isVerified(); }
            
            @Override
            public String getWebsiteUrl() { return cacheDto.getWebsiteUrl(); }
            
            @Override
            public java.time.LocalDateTime getCreatedAt() { return cacheDto.getCreatedAt(); }
            
            @Override
            public String getLicenseUrl() { return cacheDto.getLicenseUrl(); }
            
            @Override
            public String getIdProofUrl() { return cacheDto.getIdProofUrl(); }
            
            @Override
            public Double getLowestPrice() { return cacheDto.getLowestPrice(); }
            
            @Override
            public String getPhotoUrls() { return cacheDto.getPhotoUrls() != null && !cacheDto.getPhotoUrls().isEmpty() ? String.join(",", cacheDto.getPhotoUrls()) : null; }
            
            @Override
            public String getPhotoUrl() { return cacheDto.getPhotoUrl(); }
            
            @Override
            public Double getAvgRating() { return null; } // HotelTopCacheDto doesn't have averageRating field
        };
    }
}
