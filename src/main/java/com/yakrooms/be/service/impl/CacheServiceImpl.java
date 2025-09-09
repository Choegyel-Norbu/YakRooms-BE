package com.yakrooms.be.service.impl;

import com.yakrooms.be.dto.cache.*;
import com.yakrooms.be.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of cache service using DTOs for Redis operations
 * Provides clean separation between JPA entities and cache storage
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private ObjectMapper cacheObjectMapper;

    // Hotel Details Cache Operations
    @Override
    public Optional<HotelDetailsCacheDto> getHotelDetailsFromCache(Long hotelId) {
        try {
            Cache cache = cacheManager.getCache("hotelDetails");
            if (cache != null) {
                HotelDetailsCacheDto cached = cache.get(hotelId, HotelDetailsCacheDto.class);
                if (cached != null) {
                    log.debug("Retrieved hotel details from cache for ID: {}", hotelId);
                    return Optional.of(cached);
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving hotel details from cache for ID: {}, error: {}", hotelId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putHotelDetailsInCache(Long hotelId, HotelDetailsCacheDto hotelDetails) {
        try {
            Cache cache = cacheManager.getCache("hotelDetails");
            if (cache != null) {
                cache.put(hotelId, hotelDetails);
                log.debug("Stored hotel details in cache for ID: {}", hotelId);
            }
        } catch (Exception e) {
            log.warn("Error storing hotel details in cache for ID: {}, error: {}", hotelId, e.getMessage());
        }
    }

    @Override
    public void evictHotelDetailsFromCache(Long hotelId) {
        try {
            Cache cache = cacheManager.getCache("hotelDetails");
            if (cache != null) {
                cache.evict(hotelId);
                log.debug("Evicted hotel details from cache for ID: {}", hotelId);
            }
        } catch (Exception e) {
            log.warn("Error evicting hotel details from cache for ID: {}, error: {}", hotelId, e.getMessage());
        }
    }

    // Hotel Listings Cache Operations
    @Override
    public Optional<PageCacheDto<HotelListingCacheDto>> getHotelListingsFromCache(Pageable pageable) {
        try {
            Cache cache = cacheManager.getCache("hotelListings");
            if (cache != null) {
                String key = generatePageableKey(pageable);
                @SuppressWarnings("unchecked")
                PageCacheDto<HotelListingCacheDto> cached = cache.get(key, PageCacheDto.class);
                if (cached != null) {
                    log.debug("Retrieved hotel listings from cache for key: {}", key);
                    return Optional.of(cached);
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving hotel listings from cache, error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putHotelListingsInCache(Pageable pageable, PageCacheDto<HotelListingCacheDto> hotelListings) {
        try {
            Cache cache = cacheManager.getCache("hotelListings");
            if (cache != null) {
                String key = generatePageableKey(pageable);
                cache.put(key, hotelListings);
                log.debug("Stored hotel listings in cache for key: {}", key);
            }
        } catch (Exception e) {
            log.warn("Error storing hotel listings in cache, error: {}", e.getMessage());
        }
    }

    @Override
    public void evictHotelListingsFromCache() {
        try {
            Cache cache = cacheManager.getCache("hotelListings");
            if (cache != null) {
                cache.clear();
                log.debug("Evicted all hotel listings from cache");
            }
        } catch (Exception e) {
            log.warn("Error evicting hotel listings from cache, error: {}", e.getMessage());
        }
    }

    // Hotel Search Cache Operations
    @Override
    public Optional<PageCacheDto<HotelSearchCacheDto>> getHotelSearchFromCache(String district, String locality, 
                                                                             String hotelType, int page, int size) {
        try {
            Cache cache = cacheManager.getCache("searchResults");
            if (cache != null) {
                String key = generateSearchKey(district, locality, hotelType, page, size);
                @SuppressWarnings("unchecked")
                PageCacheDto<HotelSearchCacheDto> cached = cache.get(key, PageCacheDto.class);
                if (cached != null) {
                    log.debug("Retrieved hotel search from cache for key: {}", key);
                    return Optional.of(cached);
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving hotel search from cache, error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putHotelSearchInCache(String district, String locality, String hotelType, int page, int size,
                                     PageCacheDto<HotelSearchCacheDto> searchResults) {
        try {
            Cache cache = cacheManager.getCache("searchResults");
            if (cache != null) {
                String key = generateSearchKey(district, locality, hotelType, page, size);
                cache.put(key, searchResults);
                log.debug("Stored hotel search in cache for key: {}", key);
            }
        } catch (Exception e) {
            log.warn("Error storing hotel search in cache, error: {}", e.getMessage());
        }
    }

    @Override
    public void evictHotelSearchFromCache() {
        try {
            Cache cache = cacheManager.getCache("searchResults");
            if (cache != null) {
                cache.clear();
                log.debug("Evicted all hotel search from cache");
            }
        } catch (Exception e) {
            log.warn("Error evicting hotel search from cache, error: {}", e.getMessage());
        }
    }

    // Top Hotels Cache Operations
    @Override
    public Optional<List<HotelTopCacheDto>> getTopHotelsFromCache() {
        try {
            Cache cache = cacheManager.getCache("topHotels");
            if (cache != null) {
                // Always start with Object retrieval to avoid Spring Cache type casting issues
                Object raw = cache.get("top3", Object.class);
                if (raw != null) {
                    // First check if it's already the correct type
                    if (raw instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> rawList = (List<Object>) raw;
                        if (!rawList.isEmpty() && rawList.get(0) instanceof HotelTopCacheDto) {
                            @SuppressWarnings("unchecked")
                            List<HotelTopCacheDto> typed = (List<HotelTopCacheDto>) raw;
                            log.debug("Retrieved top hotels from cache (direct cast)");
                            return Optional.of(typed);
                        }
                        
                        // If not proper DTOs, try ObjectMapper conversion
                        if (cacheObjectMapper != null) {
                            try {
                                log.debug("Attempting to convert cached object to List<HotelTopCacheDto>");
                                List<HotelTopCacheDto> converted = rawList.stream()
                                    .map(obj -> cacheObjectMapper.convertValue(obj, HotelTopCacheDto.class))
                                    .collect(java.util.stream.Collectors.toList());
                                
                                if (!converted.isEmpty()) {
                                    log.debug("Successfully converted cached top hotels");
                                    // Replace the cache entry with the properly typed objects
                                    cache.put("top3", converted);
                                    return Optional.of(converted);
                                }
                            } catch (Exception conversionException) {
                                log.warn("Failed to convert cached top hotels: {}", conversionException.getMessage());
                                
                                // If conversion fails, evict the corrupted cache entry
                                try {
                                    cache.evict("top3");
                                    log.debug("Evicted corrupted top hotels cache entry");
                                } catch (Exception evictException) {
                                    log.warn("Failed to evict corrupted top hotels cache entry: {}", evictException.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving top hotels from cache, error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putTopHotelsInCache(List<HotelTopCacheDto> topHotels) {
        try {
            Cache cache = cacheManager.getCache("topHotels");
            if (cache != null) {
                cache.put("top3", topHotels);
                log.debug("Stored top hotels in cache");
            }
        } catch (Exception e) {
            log.warn("Error storing top hotels in cache, error: {}", e.getMessage());
        }
    }

    @Override
    public void evictTopHotelsFromCache() {
        try {
            Cache cache = cacheManager.getCache("topHotels");
            if (cache != null) {
                cache.clear();
                log.debug("Evicted all top hotels from cache");
            }
        } catch (Exception e) {
            log.warn("Error evicting top hotels from cache, error: {}", e.getMessage());
        }
    }

    // User Hotels Cache Operations
    @Override
    public Optional<HotelListingCacheDto> getUserHotelsFromCache(Long userId) {
        try {
            Cache cache = cacheManager.getCache("userHotels");
            if (cache != null) {
                HotelListingCacheDto cached = cache.get(userId, HotelListingCacheDto.class);
                if (cached != null) {
                    log.debug("Retrieved user hotels from cache for user ID: {}", userId);
                    return Optional.of(cached);
                }
            }
        } catch (Exception e) {
            log.warn("Error retrieving user hotels from cache for user ID: {}, error: {}", userId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putUserHotelsInCache(Long userId, HotelListingCacheDto userHotels) {
        try {
            Cache cache = cacheManager.getCache("userHotels");
            if (cache != null) {
                cache.put(userId, userHotels);
                log.debug("Stored user hotels in cache for user ID: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Error storing user hotels in cache for user ID: {}, error: {}", userId, e.getMessage());
        }
    }

    @Override
    public void evictUserHotelsFromCache(Long userId) {
        try {
            Cache cache = cacheManager.getCache("userHotels");
            if (cache != null) {
                cache.evict(userId);
                log.debug("Evicted user hotels from cache for user ID: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Error evicting user hotels from cache for user ID: {}, error: {}", userId, e.getMessage());
        }
    }

    // Bulk Cache Operations
    @Override
    public void evictAllHotelCaches() {
        evictHotelListingsFromCache();
        evictHotelSearchFromCache();
        evictTopHotelsFromCache();
        log.info("Evicted all hotel-related caches");
    }

    @Override
    public void evictAllCaches() {
        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
            log.info("Evicted all caches");
        } catch (Exception e) {
            log.warn("Error evicting all caches, error: {}", e.getMessage());
        }
    }

    @Override
    public String getCacheStatistics() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("Cache Statistics:\n");
            
            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    stats.append(String.format("- %s: %s\n", cacheName, cache.getName()));
                }
            }
            
            return stats.toString();
        } catch (Exception e) {
            log.warn("Error getting cache statistics, error: {}", e.getMessage());
            return "Error retrieving cache statistics";
        }
    }

    // Hotel Search Page Cache Operations (New DTO-based)
    @Override
    public Optional<HotelSearchPageCacheDto> getHotelSearchPageFromCache(String district, String locality, 
                                                                        String hotelType, int page, int size) {
        try {
            String key = generateSearchPageKey(district, locality, hotelType, page, size);
            Cache cache = cacheManager.getCache("searchResults");
            if (cache != null) {
                HotelSearchPageCacheDto typed = cache.get(key, HotelSearchPageCacheDto.class);
                if (typed != null) {
                    return Optional.of(typed);
                }
                Object raw = cache.get(key, Object.class);
                if (raw != null && cacheObjectMapper != null) {
                    try {
                        HotelSearchPageCacheDto converted = cacheObjectMapper.convertValue(raw, HotelSearchPageCacheDto.class);
                        return Optional.ofNullable(converted);
                    } catch (IllegalArgumentException iae) {
                        log.warn("Failed to convert cached hotel search page: {}", iae.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get hotel search page from cache: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putHotelSearchPageInCache(String district, String locality, String hotelType, int page, int size,
                                         HotelSearchPageCacheDto searchResults) {
        try {
            String key = generateSearchPageKey(district, locality, hotelType, page, size);
            Cache cache = cacheManager.getCache("searchResults");
            if (cache != null) {
                cache.put(key, searchResults);
                log.debug("Stored hotel search page in cache with key: {}", key);
            }
        } catch (Exception e) {
            log.warn("Failed to store hotel search page in cache: {}", e.getMessage());
        }
    }

    @Override
    public void evictHotelSearchPageFromCache() {
        try {
            Cache cache = cacheManager.getCache("searchResults");
            if (cache != null) {
                cache.clear();
                log.debug("Cleared hotel search page cache");
            }
        } catch (Exception e) {
            log.warn("Failed to clear hotel search page cache: {}", e.getMessage());
        }
    }

    // Hotel Listings Page Cache Operations (New DTO-based)
    @Override
    public Optional<HotelListingPageCacheDto> getHotelListingsPageFromCache(String sortType, int page, int size) {
        try {
            String key = generateListingsPageKey(sortType, page, size);
            Cache cache = cacheManager.getCache("hotelListings");
            if (cache != null) {
                // Always start with Object retrieval to avoid Spring Cache type casting issues
                Object raw = cache.get(key, Object.class);
                if (raw != null) {
                    // First check if it's already the correct type
                    if (raw instanceof HotelListingPageCacheDto) {
                        log.debug("Successfully retrieved hotel listings page from cache with key: {}", key);
                        return Optional.of((HotelListingPageCacheDto) raw);
                    }
                    
                    // If not, try ObjectMapper conversion
                    if (cacheObjectMapper != null) {
                        try {
                            log.debug("Attempting to convert cached object to HotelListingPageCacheDto for key: {}", key);
                            HotelListingPageCacheDto converted = cacheObjectMapper.convertValue(raw, HotelListingPageCacheDto.class);
                            if (converted != null) {
                                log.debug("Successfully converted cached object for key: {}", key);
                                // Replace the cache entry with the properly typed object
                                cache.put(key, converted);
                                return Optional.of(converted);
                            }
                        } catch (Exception conversionException) {
                            log.warn("Failed to convert cached hotel listings page for key: {}, error: {}", key, conversionException.getMessage());
                            log.debug("Raw cached object type: {}", raw.getClass().getSimpleName());
                            
                            // If conversion fails, evict the corrupted cache entry
                            try {
                                cache.evict(key);
                                log.debug("Evicted corrupted cache entry for key: {}", key);
                            } catch (Exception evictException) {
                                log.warn("Failed to evict corrupted cache entry: {}", evictException.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get hotel listings page from cache for key: {}, error: {}", generateListingsPageKey(sortType, page, size), e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void putHotelListingsPageInCache(String sortType, int page, int size, HotelListingPageCacheDto listings) {
        try {
            String key = generateListingsPageKey(sortType, page, size);
            Cache cache = cacheManager.getCache("hotelListings");
            if (cache != null) {
                cache.put(key, listings);
                log.debug("Stored hotel listings page in cache with key: {}", key);
            }
        } catch (Exception e) {
            log.warn("Failed to store hotel listings page in cache: {}", e.getMessage());
        }
    }

    @Override
    public void evictHotelListingsPageFromCache() {
        try {
            Cache cache = cacheManager.getCache("hotelListings");
            if (cache != null) {
                cache.clear();
                log.debug("Cleared hotel listings page cache");
            }
        } catch (Exception e) {
            log.warn("Failed to clear hotel listings page cache: {}", e.getMessage());
        }
    }

    // Helper methods for generating cache keys
    private String generatePageableKey(Pageable pageable) {
        return String.format("page_%d_size_%d_sort_%s", 
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            pageable.getSort().toString());
    }

    private String generateSearchKey(String district, String locality, String hotelType, int page, int size) {
        return String.format("search_%s_%s_%s_page_%d_size_%d", 
            district != null ? district : "null",
            locality != null ? locality : "null", 
            hotelType != null ? hotelType : "null",
            page, size);
    }

    private String generateSearchPageKey(String district, String locality, String hotelType, int page, int size) {
        return String.format("search_page_%s_%s_%s_%d_%d", 
            district != null ? district : "null",
            locality != null ? locality : "null", 
            hotelType != null ? hotelType : "null",
            page, size);
    }

    private String generateListingsPageKey(String sortType, int page, int size) {
        return String.format("listings_page_%s_%d_%d", sortType, page, size);
    }
}
