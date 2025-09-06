package com.yakrooms.be.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for caching hotel data with custom TTL settings
 * Provides optimized serialization and cache management for the YakRooms application
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Autowired
    private Environment environment;

    @Value("${app.cache.hotel-details.ttl:1800000}")
    private long hotelDetailsTtl;

    @Value("${app.cache.hotel-listings.ttl:900000}")
    private long hotelListingsTtl;

    @Value("${app.cache.search-results.ttl:600000}")
    private long searchResultsTtl;

    @Value("${app.cache.top-hotels.ttl:1800000}")
    private long topHotelsTtl;

    /**
     * Configure Redis template with Jackson serialization for optimal performance
     * Only created when Redis connection is available
     */
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Create ObjectMapper with Java 8 time support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());

        // Configure Jackson serializer
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure cache manager with custom TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create ObjectMapper for cache serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());

        // Configure Jackson serializer for cache
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Default cache configuration
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(300000)) // 5 minutes default
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
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
