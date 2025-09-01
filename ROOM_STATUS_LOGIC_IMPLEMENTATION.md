# Room Status Logic Implementation

## Overview
This document describes the implementation of the new room status logic in the YakRooms application. The logic has been updated to provide more accurate and meaningful room status information based on room availability and booking status.

## Business Logic

### Room Status Rules
The new logic implements the following status determination rules:

1. **Occupied**: Room is not available AND booking status is 'CHECKED_IN'
2. **Confirmed (Not Arrived)**: Room is not available AND booking status is 'CONFIRMED'
3. **Available**: Room is available (regardless of booking status)
4. **Under Repair**: Room is not available AND no booking exists

### Date-Based Priority Ordering
When multiple bookings exist for the same room, the system selects the booking that is **closest to today's date**:

1. **Primary Criterion**: `ABS(DATEDIFF(check_in_date, CURDATE())) ASC` - Closest date to today
2. **Secondary Criterion**: `check_in_date ASC` - If dates are equidistant, earlier date wins

**Example**: For room 201 with two bookings:
- Booking A: Check-in on day 4 (4 days from today)
- Booking B: Check-in on day 10 (10 days from today)
- **Result**: Booking A is selected because |4| < |10|

## Technical Implementation

### Repository Layer Changes
The `RoomRepository` has been updated with two optimized queries:

#### 1. `getRoomStatusByHotelId()`
- Returns room status for all rooms in a hotel
- Groups bookings by room to ensure one row per room
- Applies the new status logic
- Supports pagination

#### 2. `getRoomStatusByHotelIdAndRoomNumber()`
- Returns room status for rooms matching a specific room number pattern
- Same logic as above but with room number filtering
- Useful for search functionality

### SQL Query Structure
The queries use a sophisticated approach to ensure data integrity and select the most relevant booking:

```sql
-- Subquery to get the booking closest to today's date per room
SELECT b1.room_id, b1.status, b1.user_id, b1.check_out_date, b1.guest_name
FROM booking b1
INNER JOIN (
    SELECT room_id, status, user_id, check_out_date, guest_name
    FROM (
        SELECT room_id, status, user_id, check_out_date, guest_name,
               ROW_NUMBER() OVER (PARTITION BY room_id ORDER BY 
                   ABS(DATEDIFF(check_in_date, CURDATE())) ASC,
                   check_in_date ASC) as rn
        FROM booking 
        WHERE status IN ('CHECKED_IN', 'CONFIRMED')
    ) ranked_bookings
    WHERE rn = 1
) b2 ON b1.room_id = b2.room_id 
    AND b1.status = b2.status 
    AND b1.user_id = b2.user_id 
    AND b1.check_out_date = b2.check_out_date
    AND b1.guest_name = b2.guest_name
```

**Key Components:**
- `ABS(DATEDIFF(check_in_date, CURDATE())) ASC`: Calculates absolute difference between check-in date and today, orders by smallest difference first
- `check_in_date ASC`: Secondary ordering for tiebreaking when dates are equidistant from today
- `ROW_NUMBER() OVER (PARTITION BY room_id...)`: Ensures only one booking per room is selected
- `guest_name` field: Provides fallback guest information when user name is not available

### Key Features
- **Window Functions**: Uses `ROW_NUMBER()` to rank bookings by proximity to today
- **Date-Based Selection**: `ABS(DATEDIFF(check_in_date, CURDATE()))` finds closest date
- **Tiebreaking**: If equidistant, earlier check-in date wins
- **Single Row Per Room**: Ensures each room appears only once in results

## Data Model

### RoomStatusProjection Interface
```java
public interface RoomStatusProjection {
    String getRoomNumber();
    String getRoomType();
    String getRoomStatus();
    String getGuestName();
    LocalDate getCheckOutDate();
}
```

### Status Values
- `"Occupied"` - Guest is currently in the room
- `"Confirmed (Not Arrived)"` - Guest has confirmed booking but hasn't arrived
- `"Available"` - Room is available for booking
- `"Under Repair"` - Room is not available and has no active bookings

### Guest Name Logic
The system provides guest information with comprehensive fallback logic:

- **CHECKED_IN**: 
  - Primary: User name from `users` table
  - Fallback: Guest name from `booking.guest_name` table
  - Default: "Guest"
- **CONFIRMED**: 
  - Primary: User name + "(Not Arrived)" suffix
  - Fallback: Guest name + "(Not Arrived)" suffix
  - Default: "Guest (Not Arrived)"
- **No Booking**: Shows "No guest"

**Fallback Priority**: `users.name` → `booking.guest_name` → Default text

## Testing

### Test Coverage
A comprehensive test suite has been created (`RoomRepositoryTest`) that covers:

1. **Basic Status Logic**: Verifies correct status for each scenario
2. **Room Grouping**: Ensures one row per room despite multiple bookings
3. **Date-Based Selection**: Tests that closest date to today is selected
4. **Edge Cases**: Handles rooms with no bookings, multiple bookings, etc.

### Test Scenarios
- Room with CHECKED_IN booking → "Occupied"
- Room with CONFIRMED booking → "Confirmed (Not Arrived)"
- Available room → "Available"
- Room not available, no booking → "Under Repair"
- Multiple bookings per room → Only booking closest to today's date shown

## Performance Considerations

### Database Indexes
The queries benefit from existing indexes:
- `idx_booking_room_id` on `booking.room_id`
- `idx_booking_status` on `booking.status`
- `idx_room_hotel_id` on `room.hotel_id`

### Query Optimization
- Uses window functions for efficient ranking
- Filters bookings by relevant statuses early in the query
- Leverages existing database indexes
- Supports pagination for large result sets

## Usage Examples

### Basic Room Status Query
```java
PageRequest pageRequest = PageRequest.of(0, 10);
Page<RoomStatusProjection> roomStatuses = 
    roomRepository.getRoomStatusByHotelId(hotelId, pageRequest);
```

### Room Status with Search
```java
PageRequest pageRequest = PageRequest.of(0, 10);
Page<RoomStatusProjection> roomStatuses = 
    roomRepository.getRoomStatusByHotelIdAndRoomNumber(hotelId, "101", pageRequest);
```

## Migration Notes

### Breaking Changes
- Room status values have changed from previous implementation
- "Booked" status has been replaced with "Occupied"
- "Confirmed" status now includes "(Not Arrived)" suffix

### Backward Compatibility
- API endpoints remain the same
- DTO structure unchanged
- Only the business logic and status values have been updated

## Future Enhancements

### Potential Improvements
1. **Status History**: Track status changes over time
2. **Custom Statuses**: Allow hotel-specific status definitions
3. **Real-time Updates**: WebSocket integration for live status updates
4. **Status Analytics**: Reporting on room utilization patterns

### Monitoring
- Query performance metrics
- Status distribution analytics
- Error rate monitoring for edge cases

## Conclusion

The new room status logic provides a more accurate and meaningful representation of room states while maintaining performance and scalability. The implementation ensures data consistency and provides a solid foundation for future enhancements.
