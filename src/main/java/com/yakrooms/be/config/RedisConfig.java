package com.yakrooms.be.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching DTOs with custom TTL settings
 * Simplified configuration since we're caching DTOs instead of JPA entities
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${app.cache.hotel-details.ttl:1800000}")
    private long hotelDetailsTtl;

    @Value("${app.cache.hotel-listings.ttl:900000}")
    private long hotelListingsTtl;

    @Value("${app.cache.search-results.ttl:600000}")
    private long searchResultsTtl;

    @Value("${app.cache.top-hotels.ttl:1800000}")
    private long topHotelsTtl;

    /**
     * Create ObjectMapper optimized for DTO serialization
     * Much simpler since we're not dealing with JPA entities and proxies
     */
    private ObjectMapper createCacheObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Configure visibility and features for DTOs
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Register Java 8 time module for LocalDateTime support
        objectMapper.registerModule(new JavaTimeModule());
        
        // Configure date/time serialization
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Configure to handle collections and arrays properly
        objectMapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);
        objectMapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, false);
        
        // Handle null values gracefully
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);

        return objectMapper;
    }

    /**
     * Expose the cache ObjectMapper as a bean for reuse (e.g., manual cache conversions)
     */
    @Bean(name = "cacheObjectMapper")
    public ObjectMapper cacheObjectMapper() {
        return createCacheObjectMapper();
    }

    /**
     * Configure Redis template with GenericJackson2JsonRedisSerializer for optimal performance
     * Only when Redis classes are available
     */
    @Bean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use the configured ObjectMapper
        ObjectMapper objectMapper = createCacheObjectMapper();

        // Configure GenericJackson2JsonRedisSerializer - handles proxies better
        // Use our configured ObjectMapper with JavaTimeModule support
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(genericJackson2JsonRedisSerializer);
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure Redis cache manager with custom TTL for different cache types
     * Only when Redis is configured and available
     */
    @Bean("redisCacheManager")
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Use the configured ObjectMapper
        ObjectMapper objectMapper = createCacheObjectMapper();

        // Configure GenericJackson2JsonRedisSerializer for cache - handles proxies better
        // Use our configured ObjectMapper with JavaTimeModule support
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(300000)) // 5 minutes default
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
                .disableCachingNullValues();

        // Custom cache configurations with specific TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Hotel details cache - 30 minutes
        cacheConfigurations.put("hotelDetails", defaultCacheConfig
                .entryTtl(Duration.ofMillis(hotelDetailsTtl)));
        
        // Hotel listings cache - 15 minutes  
        cacheConfigurations.put("hotelListings", defaultCacheConfig
                .entryTtl(Duration.ofMillis(hotelListingsTtl)));
        
        // Search results cache - 10 minutes
        cacheConfigurations.put("searchResults", defaultCacheConfig
                .entryTtl(Duration.ofMillis(searchResultsTtl)));
        
        // Top hotels cache - 30 minutes
        cacheConfigurations.put("topHotels", defaultCacheConfig
                .entryTtl(Duration.ofMillis(topHotelsTtl)));
        
        // User hotels cache - 15 minutes (same as listings)
        cacheConfigurations.put("userHotels", defaultCacheConfig
                .entryTtl(Duration.ofMillis(hotelListingsTtl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}