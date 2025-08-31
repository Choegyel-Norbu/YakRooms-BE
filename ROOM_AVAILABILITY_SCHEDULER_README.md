# Room Availability Scheduler

## Overview
Automated room availability management system that runs daily at 12:00 PM to update room statuses based on check-in/check-out dates.

## Key Features
- **Daily Noon Scheduler**: Automatically runs at 12:00 PM every day
- **Bulk Operations**: Efficiently updates multiple rooms in single database operations
- **Centralized Logic**: All availability updates go through `RoomAvailabilityService`
- **Transactional Safety**: Proper transaction management prevents race conditions
- **Comprehensive Logging**: Full traceability of all availability changes

## How It Works

### 1. Checkout Completions (Make Rooms Available)
- Finds bookings where `check_out_date = today` and `status IN (CONFIRMED, CHECKED_IN)`
- Sets room availability to `true`
- Reason: Guest has checked out, room is now available

### 2. Checkin Starts (Make Rooms Unavailable)
- Finds bookings where `check_in_date = today` and `status IN (CONFIRMED, PENDING)`
- Sets room availability to `false`
- Reason: Guest is checking in today, room should be blocked

## Architecture

```
RoomAvailabilityScheduler (Daily at 12:00 PM)
    ↓
RoomAvailabilityService.processDailyRoomAvailabilityUpdates()
    ↓
1. Process checkout completions → Make rooms available
2. Process checkin starts → Make rooms unavailable
```

## Database Optimization
- New indexes on `(check_out_date, status)` and `(check_in_date, status)`
- Bulk update queries for efficient room availability updates
- Composite indexes for room-specific queries

## Integration
- **New Bookings**: Updates availability based on check-in date/time
- **Cancelled Bookings**: Restores availability if appropriate
- **Check-in/Check-out**: Immediate availability updates
- **Manual Trigger**: Available for testing or immediate updates

## Configuration
```java
@Scheduled(cron = "0 0 12 * * ?") // Every day at 12:00 PM
@EnableScheduling // Required in main application class
```

## Testing
- Comprehensive unit tests for all scenarios
- Mock time testing for different check-in scenarios
- Error handling and edge case coverage

## Monitoring
- Detailed logging of all operations
- Performance metrics (rooms updated, processing time)
- Error tracking and alerting

## Files Created/Modified
- `RoomAvailabilityService.java` - Service interface
- `RoomAvailabilityServiceImpl.java` - Service implementation
- `RoomAvailabilityScheduler.java` - Daily scheduler
- `V6__add_room_availability_scheduler_indexes.sql` - Database indexes
- `RoomAvailabilityServiceTest.java` - Comprehensive tests
- Updated `UnifiedBookingServiceImpl.java` - Integration
- Updated `YakroomsApplication.java` - Enable scheduling
