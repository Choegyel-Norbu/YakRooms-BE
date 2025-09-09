package com.yakrooms.be.service;

import com.yakrooms.be.dto.cache.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Redis cache operations with DTOs
 * Provides type-safe cache operations without JPA/Hibernate dependencies
 */
public interface CacheService {

    // Hotel Details Cache Operations
    Optional<HotelDetailsCacheDto> getHotelDetailsFromCache(Long hotelId);
    void putHotelDetailsInCache(Long hotelId, HotelDetailsCacheDto hotelDetails);
    void evictHotelDetailsFromCache(Long hotelId);

    // Hotel Listings Cache Operations
    Optional<PageCacheDto<HotelListingCacheDto>> getHotelListingsFromCache(Pageable pageable);
    void putHotelListingsInCache(Pageable pageable, PageCacheDto<HotelListingCacheDto> hotelListings);
    void evictHotelListingsFromCache();

    // Hotel Search Cache Operations
    Optional<PageCacheDto<HotelSearchCacheDto>> getHotelSearchFromCache(String district, String locality, 
                                                                       String hotelType, int page, int size);
    void putHotelSearchInCache(String district, String locality, String hotelType, int page, int size,
                              PageCacheDto<HotelSearchCacheDto> searchResults);
    void evictHotelSearchFromCache();

    // Hotel Search Page Cache Operations (New DTO-based)
    Optional<HotelSearchPageCacheDto> getHotelSearchPageFromCache(String district, String locality, 
                                                                 String hotelType, int page, int size);
    void putHotelSearchPageInCache(String district, String locality, String hotelType, int page, int size,
                                  HotelSearchPageCacheDto searchResults);
    void evictHotelSearchPageFromCache();

    // Hotel Listings Page Cache Operations (New DTO-based)
    Optional<HotelListingPageCacheDto> getHotelListingsPageFromCache(String sortType, int page, int size);
    void putHotelListingsPageInCache(String sortType, int page, int size, HotelListingPageCacheDto listings);
    void evictHotelListingsPageFromCache();

    // Top Hotels Cache Operations
    Optional<List<HotelTopCacheDto>> getTopHotelsFromCache();
    void putTopHotelsInCache(List<HotelTopCacheDto> topHotels);
    void evictTopHotelsFromCache();

    // User Hotels Cache Operations
    Optional<HotelListingCacheDto> getUserHotelsFromCache(Long userId);
    void putUserHotelsInCache(Long userId, HotelListingCacheDto userHotels);
    void evictUserHotelsFromCache(Long userId);

    // Bulk Cache Operations
    void evictAllHotelCaches();
    void evictAllCaches();
    
    // Cache Statistics
    String getCacheStatistics();
}
