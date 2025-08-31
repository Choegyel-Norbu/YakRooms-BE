# Room Availability Scheduler Implementation

## Overview

This document describes the implementation of a comprehensive room availability management system for YakRooms. The system includes a scheduled job that runs daily at noon (12:00 PM) to automatically update room availability based on check-in and check-out dates.

## Architecture Overview

### Core Components

1. **RoomAvailabilityService** - Centralized service for all room availability logic
2. **RoomAvailabilityScheduler** - Spring Boot scheduled job that runs daily at noon
3. **Database Indexes** - Optimized queries for efficient bulk operations
4. **Transactional Safety** - Proper transaction management to prevent race conditions

### Design Principles

- **Separation of Concerns**: Scheduler only triggers the service, no business logic
- **Centralized Logic**: All availability updates go through RoomAvailabilityService
- **Idempotent Operations**: Safe to run multiple times without side effects
- **Bulk Operations**: Efficient database updates for multiple rooms
- **Comprehensive Logging**: Full traceability of all availability changes

## Implementation Details

### 1. RoomAvailabilityService Interface

```java
public interface RoomAvailabilityService {
    int processDailyRoomAvailabilityUpdates();
    boolean updateRoomAvailabilityForNewBooking(Long roomId, LocalDate checkInDate, LocalTime checkInTime);
    boolean updateRoomAvailabilityForCancelledBooking(Long roomId, LocalDate checkInDate);
    boolean updateRoomAvailabilityForConfirmedBooking(Long roomId, LocalDate checkInDate);
    boolean updateRoomAvailabilityForCheckIn(Long roomId);
    boolean updateRoomAvailabilityForCheckOut(Long roomId);
}
```

### 2. Daily Scheduler Logic

The scheduler runs every day at 12:00 PM and performs two main operations:

#### Step 1: Make Rooms Available (Checkout Completed)
- **Query**: Find bookings where `check_out_date = today` and `status IN (CONFIRMED, CHECKED_IN)`
- **Action**: Set room availability to `true`
- **Reason**: Guest has checked out, room is now available for new bookings

#### Step 2: Make Rooms Unavailable (Checkin Starting)
- **Query**: Find bookings where `check_in_date = today` and `status IN (CONFIRMED, PENDING)`
- **Action**: Set room availability to `false`
- **Reason**: Guest is checking in today, room should be blocked

### 3. Cron Expression

```java
@Scheduled(cron = "0 0 12 * * ?") // Every day at 12:00 PM
```

**Cron Breakdown**:
- `0` - Second (0)
- `0` - Minute (0)
- `12` - Hour (12 = noon)
- `*` - Day of month (any)
- `*` - Month (any)
- `?` - Day of week (no specific value)

### 4. Database Optimization

#### New Indexes Added

```sql
-- For checkout completions
CREATE INDEX idx_booking_checkout_status ON booking (check_out_date, status);

-- For checkin starts
CREATE INDEX idx_booking_checkin_status ON booking (check_in_date, status);

-- Composite indexes for room-specific queries
CREATE INDEX idx_booking_room_checkout_status ON booking (room_id, check_out_date, status);
CREATE INDEX idx_booking_room_checkin_status ON booking (room_id, check_in_date, status);

-- For bulk room updates
CREATE INDEX idx_room_hotel_available ON room (hotel_id, is_available);
```

#### Bulk Update Queries

```java
@Modifying
@Query("UPDATE Room r SET r.isAvailable = :available WHERE r.id IN :roomIds")
int bulkUpdateRoomAvailability(@Param("roomIds") List<Long> roomIds, @Param("available") boolean available);
```

### 5. Transactional Safety

All availability updates are wrapped in `@Transactional` blocks to ensure:
- **Atomicity**: All updates succeed or fail together
- **Consistency**: Database remains in a valid state
- **Isolation**: Concurrent operations don't interfere
- **Durability**: Changes are permanently saved

## Integration Points

### 1. Booking Workflows

The service integrates with existing booking operations:

- **New Booking**: Updates availability based on check-in date/time
- **Cancelled Booking**: Restores availability if appropriate
- **Confirmed Booking**: Updates availability based on check-in date
- **Check-in**: Makes room unavailable
- **Check-out**: Makes room available

### 2. Manual Triggers

```java
// Manual trigger for testing or immediate updates
public void manualRoomAvailabilityUpdate() {
    int updatedRooms = roomAvailabilityService.processDailyRoomAvailabilityUpdates();
    logger.info("Manual update completed. {} rooms updated.", updatedRooms);
}
```

## Performance Characteristics

### 1. Query Efficiency

- **Selective Queries**: Only fetch room IDs, not full entities
- **Bulk Updates**: Update multiple rooms in single database operations
- **Indexed Queries**: All scheduler queries use optimized indexes

### 2. Scalability

- **Bulk Operations**: Handle hundreds of rooms efficiently
- **Minimal Database Load**: Only necessary data is transferred
- **Asynchronous Processing**: Non-blocking operations

### 3. Monitoring

- **Comprehensive Logging**: Every operation is logged with details
- **Performance Metrics**: Track number of rooms updated
- **Error Handling**: Graceful failure with detailed error messages

## Configuration

### 1. Enable Scheduling

```java
@SpringBootApplication
@EnableScheduling  // Required for @Scheduled annotations
public class YakroomsApplication {
    // ... existing code
}
```

### 2. Time Zone Considerations

The scheduler uses the server's default time zone. For production:
- Ensure server time zone is correctly configured
- Consider using `@Scheduled(cron = "...", zone = "UTC")` for consistency

### 3. Environment-Specific Settings

```properties
# application.properties
spring.task.scheduling.pool.size=5
logging.level.com.yakrooms.be.service.RoomAvailabilityService=INFO
```

## Testing

### 1. Unit Tests

Comprehensive test coverage includes:
- Daily scheduler operations
- Individual booking scenarios
- Error handling and edge cases
- Mock time scenarios

### 2. Integration Tests

Test the complete flow:
- Database operations
- Transaction rollbacks
- Concurrent access scenarios

### 3. Manual Testing

```bash
# Trigger manual update via REST endpoint (if exposed)
curl -X POST /api/admin/room-availability/update

# Check logs for operation details
tail -f logs/application.log | grep "Room availability"
```

## Monitoring and Observability

### 1. Logging Levels

- **INFO**: Successful operations with counts
- **DEBUG**: Detailed operation steps
- **ERROR**: Failed operations with stack traces

### 2. Key Metrics

- Number of rooms made available
- Number of rooms made unavailable
- Total processing time
- Error rates

### 3. Health Checks

Monitor scheduler health:
- Last successful run time
- Last run duration
- Error counts

## Troubleshooting

### 1. Common Issues

**Scheduler Not Running**:
- Check `@EnableScheduling` annotation
- Verify cron expression syntax
- Check application logs for errors

**Performance Issues**:
- Verify database indexes are created
- Check query execution plans
- Monitor database connection pool

**Data Inconsistencies**:
- Check transaction boundaries
- Verify rollback scenarios
- Review concurrent access patterns

### 2. Debug Mode

Enable debug logging:
```properties
logging.level.com.yakrooms.be.service.RoomAvailabilityService=DEBUG
logging.level.com.yakrooms.be.service.RoomAvailabilityScheduler=DEBUG
```

## Future Enhancements

### 1. Configurable Timing

- Make check-in/check-out times configurable
- Support multiple daily runs
- Time zone-aware scheduling

### 2. Advanced Logic

- Handle partial day bookings
- Support for early check-ins
- Late check-out scenarios

### 3. Integration Features

- Webhook notifications for availability changes
- Real-time availability updates via WebSocket
- Integration with external PMS systems

## Conclusion

This implementation provides a robust, scalable solution for automatic room availability management. The system is designed to handle real-world scenarios efficiently while maintaining data consistency and providing comprehensive monitoring capabilities.

The architecture follows Spring Boot best practices and enterprise patterns, making it suitable for production environments with high booking volumes.
