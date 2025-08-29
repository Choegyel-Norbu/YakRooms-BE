# Redis Caching Implementation for YakRooms

## Overview

This document describes the comprehensive Redis caching implementation for the YakRooms Spring Boot application. The implementation provides a multi-layered caching strategy to improve performance, reduce database load, and enhance user experience.

## Architecture

### Cache Layers

1. **Application-level caching** with Spring Cache + Redis
2. **Service-level caching** for business logic results
3. **Controller-level caching** for HTTP responses
4. **Cache invalidation strategies** for data consistency

### Cache Types

- **hotelDetails**: Individual hotel information (TTL: 30 minutes)
- **hotelListings**: Paginated hotel lists (TTL: 15 minutes)
- **searchResults**: Hotel search results (TTL: 10 minutes)
- **topHotels**: Top three hotels (TTL: 30 minutes)
- **userHotels**: User-specific hotel listings (TTL: 15 minutes)

## Configuration

### Dependencies Added

```xml
<!-- Redis Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>

<!-- Spring Boot Actuator for monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Redis Configuration

```properties
# Redis Configuration
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.database=${REDIS_DATABASE:0}
spring.data.redis.timeout=2000ms

# Redis Connection Pool Configuration
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
spring.data.redis.lettuce.pool.max-wait=-1ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
spring.cache.redis.cache-null-values=false
spring.cache.redis.use-key-prefix=true
spring.cache.redis.key-prefix=yakrooms:
```

## Implementation Details

### 1. Redis Configuration Class

The `RedisConfig` class provides:
- Custom serialization with Jackson
- Type-safe caching with JSR310 time module support
- Custom TTL configurations for different cache types
- Connection pool optimization

### 2. Cache Service

The `CacheService` provides advanced cache operations:
- Manual cache management
- Cache invalidation strategies
- Cache statistics and monitoring
- Cache warm-up capabilities

### 3. Service Layer Caching

Hotel service methods are annotated with caching:

```java
@Cacheable(value = "hotelDetails", key = "#hotelId")
public HotelResponse getHotelById(Long hotelId)

@Cacheable(value = "hotelListings", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
public Page<HotelWithLowestPriceProjection> getAllHotels(Pageable pageable)

@Cacheable(value = "searchResults", key = "'search_' + #district + '_' + #locality + '_' + #hotelType + '_' + #page + '_' + #size")
public Page<HotelWithLowestPriceProjection> searchHotels(...)
```

### 4. Cache Eviction

Data modification methods automatically evict related caches:

```java
@Caching(evict = {
    @CacheEvict(value = "hotelDetails", key = "#id"),
    @CacheEvict(value = {"hotelListings", "searchResults", "topHotels"}, allEntries = true)
})
public HotelResponse updateHotel(Long id, HotelRequest request)
```

## Cache Management API

### Endpoints

- `GET /api/admin/cache/stats` - Get cache statistics
- `DELETE /api/admin/cache/{cacheName}` - Clear specific cache
- `DELETE /api/admin/cache/hotels/all` - Clear all hotel caches
- `DELETE /api/admin/cache/hotels/{hotelId}` - Clear caches for specific hotel
- `DELETE /api/admin/cache/users/{userId}` - Clear user-specific caches
- `POST /api/admin/cache/warmup` - Warm up cache
- `GET /api/admin/cache/health` - Get cache health status

### Monitoring

- Spring Boot Actuator endpoints for cache metrics
- Custom cache statistics and health checks
- Redis connection monitoring

## Docker Deployment

### Docker Compose

```yaml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD:-}
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Environment Variables

```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password
REDIS_DATABASE=0
```

## Performance Benefits

### Expected Improvements

1. **Response Time**: 60-80% reduction for cached operations
2. **Database Load**: 70-90% reduction in database queries
3. **Scalability**: Better handling of concurrent users
4. **User Experience**: Faster page loads and search results

### Cache Hit Ratios

- **Hotel Details**: 85-95% (highly cacheable)
- **Hotel Listings**: 70-85% (moderate cacheability)
- **Search Results**: 60-75% (query-dependent)
- **Top Hotels**: 90-98% (very cacheable)

## Best Practices

### 1. Cache Key Design

- Use descriptive, unique keys
- Include pagination and sorting parameters
- Avoid overly complex key structures

### 2. TTL Strategy

- **Short TTL** (5-10 min): Frequently changing data
- **Medium TTL** (15-30 min): Moderately stable data
- **Long TTL** (1+ hours): Rarely changing data

### 3. Cache Invalidation

- **Write-through**: Immediate invalidation on updates
- **Time-based**: Automatic expiration
- **Event-driven**: Invalidation based on business events

### 4. Memory Management

- Monitor Redis memory usage
- Set appropriate maxmemory policies
- Use Redis persistence for data durability

## Monitoring and Maintenance

### Health Checks

- Redis connection status
- Cache hit/miss ratios
- Memory usage monitoring
- Performance metrics

### Maintenance Tasks

- Regular cache warming
- Cache statistics review
- Memory optimization
- Performance tuning

## Troubleshooting

### Common Issues

1. **Cache Misses**: Check TTL settings and invalidation logic
2. **Memory Issues**: Monitor Redis memory usage and eviction policies
3. **Serialization Errors**: Verify Jackson configuration and entity serialization
4. **Connection Issues**: Check Redis connectivity and connection pool settings

### Debug Mode

Enable debug logging for cache operations:

```properties
logging.level.org.springframework.cache=DEBUG
logging.level.org.springframework.data.redis=DEBUG
```

## Future Enhancements

### Planned Features

1. **Distributed Caching**: Redis Cluster for high availability
2. **Cache Warming**: Intelligent pre-loading of frequently accessed data
3. **Advanced Metrics**: Custom cache performance indicators
4. **Cache Patterns**: Implementation of cache-aside and write-through patterns

### Scalability Considerations

1. **Horizontal Scaling**: Redis Cluster deployment
2. **Load Balancing**: Multiple Redis instances
3. **Data Partitioning**: Sharding strategies for large datasets
4. **Backup and Recovery**: Redis persistence and replication

## Conclusion

This Redis caching implementation provides a robust, scalable, and maintainable caching solution for the YakRooms application. It significantly improves performance while maintaining data consistency and providing comprehensive monitoring capabilities.

The implementation follows Spring Boot best practices and provides a solid foundation for future caching enhancements and optimizations.
