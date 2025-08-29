# Advance Booking Implementation

## Overview

This document describes the implementation of advance booking functionality in the YakRooms application. The advance booking system allows users to check room availability for future dates and specifically handles conflicts with existing CONFIRMED bookings.

## Architecture Design

### Problem Analysis
The advance booking system addresses the need to:
- Check room availability for future dates
- Handle conflicts with existing confirmed bookings
- Provide a clear distinction between immediate availability and advance booking availability
- Support business logic for future reservations

### System Design
The implementation follows a layered architecture approach:

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database Layer (MySQL)
```

## Implementation Details

### 1. Repository Layer (`BookingRepository.java`)

#### New Query Method
```java
@Query("""
    SELECT b FROM Booking b 
    WHERE b.room.id = :roomId 
    AND b.status = 'CONFIRMED'
    AND ( (b.checkInDate < :requestedCheckOut) AND (b.checkOutDate > :requestedCheckIn) )
    """)
List<Booking> findAdvanceBookingConflicts(@Param("roomId") Long roomId,
                                         @Param("requestedCheckIn") LocalDate requestedCheckIn,
                                         @Param("requestedCheckOut") LocalDate requestedCheckOut);
```

**Key Features:**
- **Status Filtering**: Only checks CONFIRMED bookings (not PENDING or CHECKED_IN)
- **Date Overlap Logic**: Uses the overlap algorithm `(checkIn < requestedCheckOut) AND (checkOut > requestedCheckIn)`
- **Performance**: Leverages existing database indexes on `room_id`, `status`, and date columns

#### Query Logic Explanation
The overlap detection works as follows:
- **Requested Period**: `[requestedCheckIn, requestedCheckOut]`
- **Existing Booking**: `[checkIn, checkOut]`
- **Conflict Exists When**: The periods overlap, meaning:
  - Existing check-in is before requested check-out AND
  - Existing check-out is after requested check-in

### 2. Service Layer (`BookingService.java` & `BookingServiceImpl.java`)

#### Interface Method
```java
boolean isRoomAvailableForAdvanceBooking(Long roomId, LocalDate requestedCheckIn, LocalDate requestedCheckOut);
```

#### Implementation Features
- **Date Validation**: Ensures requested dates are in the future
- **Business Rule Enforcement**: Prevents past date bookings
- **Conflict Resolution**: Returns availability based on existing confirmed bookings
- **Logging**: Comprehensive logging for debugging and monitoring
- **NEW: Automatic Booking Creation**: Creates PENDING bookings when rooms are available

#### Business Logic
```java
@Override
@Transactional
public boolean isRoomAvailableForAdvanceBooking(Long roomId, LocalDate requestedCheckIn, LocalDate requestedCheckOut) {
    // Validate date range
    validateDateRange(requestedCheckIn, requestedCheckOut);
    
    // Ensure dates are in the future
    LocalDate today = LocalDate.now();
    if (requestedCheckIn.isBefore(today)) {
        throw new BusinessException("Check-in date cannot be in the past for advance bookings");
    }
    
    // Check for conflicts with existing CONFIRMED bookings
    List<Booking> conflictingBookings = bookingRepository.findAdvanceBookingConflicts(
        roomId, requestedCheckIn, requestedCheckOut);
    
    boolean isAvailable = conflictingBookings.isEmpty();
    
    // If room is available, create a pending booking
    if (isAvailable) {
        try {
            // Fetch required entities
            Room room = fetchRoomById(roomId);
            Hotel hotel = fetchHotelById(room.getHotel().getId());
            
            // Create a new pending booking
            Booking advanceBooking = new Booking();
            advanceBooking.setRoom(room);
            advanceBooking.setHotel(hotel);
            advanceBooking.setCheckInDate(requestedCheckIn);
            advanceBooking.setCheckOutDate(requestedCheckOut);
            advanceBooking.setStatus(BookingStatus.PENDING);
            advanceBooking.setPaymentStatus(PaymentStatus.PENDING);
            
            // Calculate total price based on room price and duration
            long days = requestedCheckIn.until(requestedCheckOut).getDays();
            advanceBooking.setTotalPrice(BigDecimal.valueOf(room.getPrice() * days));
            
            // Generate unique passcode
            advanceBooking.setPasscode(generateUniquePasscode());
            
            // Save the advance booking
            Booking savedBooking = bookingRepository.save(advanceBooking);
            
            // Update room availability
            updateRoomAvailability(room, false);
            
            logger.info("Successfully created pending advance booking with ID: {}", savedBooking.getId());
            
        } catch (Exception e) {
            logger.error("Failed to create advance booking for room: {}", roomId, e);
            throw new BusinessException("Failed to create advance booking: " + e.getMessage());
        }
    }
    
    return isAvailable;
}
```

### 3. Controller Layer (`BookingController.java`)

#### New Endpoint
```java
@PostMapping("/availability/advance")
public ResponseEntity<Map<String, Object>> checkAdvanceBookingAvailabilityWithDTO(
        @Valid @RequestBody BookingRequest request)
```

**Endpoint Details:**
- **URL**: `POST /api/bookings/availability/advance`
- **Access**: Public (no authentication required)
- **Parameters**: 
  - `roomId`: Long - The room identifier
  - `requestedCheckIn`: LocalDate - Requested check-in date
  - `requestedCheckOut`: LocalDate - Requested check-out date
- **Response**: JSON with availability status, booking creation result, and metadata

#### Response Format
**Success Response (Room Available - Booking Created):**
```json
{
  "roomId": 1,
  "requestedCheckIn": "2024-02-01",
  "requestedCheckOut": "2024-02-05",
  "isAvailable": true,
  "message": "Room is available and pending booking has been created",
  "bookingStatus": "PENDING",
  "action": "Advance booking created successfully",
  "hotelId": 1,
  "guests": 2,
  "numberOfRooms": 1
}
```

**Success Response (Room Not Available - No Booking Created):**
```json
{
  "roomId": 1,
  "requestedCheckIn": "2024-02-01",
  "requestedCheckOut": "2024-02-05",
  "isAvailable": false,
  "message": "Room is not available for the requested dates",
  "bookingStatus": "UNAVAILABLE",
  "action": "No booking created - conflicts detected"
}
```

**Error Response:**
```json
{
  "error": "Check-in date cannot be in the past for advance bookings",
  "roomId": 1,
  "requestedCheckIn": "2023-01-01",
  "requestedCheckOut": "2023-01-05"
}
```

## Automatic Pending Booking Creation

### How It Works
When a room is available for advance booking:

1. **Availability Check**: The system checks for conflicts with existing CONFIRMED bookings
2. **Conflict Resolution**: If no conflicts are found, the room is marked as available
3. **Automatic Creation**: A new PENDING booking is automatically created with:
   - **Status**: `PENDING` (awaiting confirmation)
   - **Payment Status**: `PENDING` (awaiting payment)
   - **Unique Passcode**: Generated for guest access
   - **Total Price**: Calculated based on room price and duration
4. **Room Management**: Room availability is updated to reflect the new booking
5. **Response**: Returns success with booking creation confirmation

### Benefits
- **Immediate Reservation**: Guests can secure rooms instantly
- **No Double-Booking**: Prevents conflicts with future requests
- **Streamlined Process**: Single API call handles both availability check and booking
- **Business Logic**: Follows hotel industry practices for advance reservations

### Use Cases
- **Online Booking Systems**: Immediate room reservation
- **Travel Agencies**: Bulk booking management
- **Corporate Bookings**: Advance planning for business travel
- **Event Planning**: Securing rooms for special events

## Key Differences from Regular Availability

### Regular Availability (`/availability`)
- Checks ALL booking statuses (PENDING, CONFIRMED, CHECKED_IN)
- Used for immediate/imminent bookings
- Includes current room state

### Advance Availability (`/availability/advance`)
- Only checks CONFIRMED bookings
- Used for future date planning
- Excludes temporary states (PENDING, CHECKED_IN)
- **NEW**: Automatically creates PENDING bookings when rooms are available

## Database Performance Considerations

### Indexing Strategy
The implementation leverages existing indexes:
```sql
-- Composite index for room availability queries
CREATE INDEX idx_booking_dates ON booking (check_in_date, check_out_date);

-- Status-based filtering
CREATE INDEX idx_booking_status ON booking (status);

-- Room-specific queries
CREATE INDEX idx_booking_room_id ON booking (room_id);
```

### Query Optimization
- **Selective Filtering**: Only fetches necessary booking data
- **Index Usage**: Leverages composite indexes for date range queries
- **Minimal Data Transfer**: Returns only conflict information, not full booking details

## Testing Strategy

### Unit Tests
- Service layer business logic validation
- Repository query correctness
- Date validation edge cases

### Integration Tests
- End-to-end API testing
- Database query performance
- Error handling scenarios

### Test Scenarios
1. **Valid Future Dates**: Room available for advance booking
2. **Past Dates**: Proper error handling
3. **Date Range Validation**: Invalid check-in/check-out combinations
4. **Conflict Detection**: Existing confirmed bookings
5. **Performance**: Large dataset handling

## Security Considerations

### Input Validation
- Date format validation
- Range validation (check-out > check-in)
- Future date enforcement

### Access Control
- Public endpoint for availability checking
- No sensitive data exposure
- Rate limiting considerations

## Monitoring and Observability

### Logging
```java
logger.info("Checking advance booking availability for room: {}, checkIn: {}, checkOut: {}", 
           roomId, requestedCheckIn, requestedCheckOut);

logger.info("Advance booking availability for room {}: {} (conflicts found: {})", 
           roomId, isAvailable, conflictingBookings.size());
```

### Metrics to Track
- Response times for availability checks
- Conflict detection accuracy
- Error rates for invalid date ranges
- Usage patterns by date ranges

## Future Enhancements

### Potential Improvements
1. **Caching**: Implement Redis caching for frequently requested date ranges
2. **Batch Processing**: Support multiple room availability checks
3. **Advanced Filtering**: Add filters for room types, pricing, amenities
4. **Predictive Analytics**: Suggest optimal booking dates based on historical data

### Scalability Considerations
- **Database Partitioning**: Partition by date ranges for large datasets
- **Read Replicas**: Use read replicas for availability queries
- **Async Processing**: Implement async conflict resolution for high-volume scenarios

## Usage Examples

### Basic Availability Check
```bash
curl -X GET "http://localhost:8080/api/bookings/availability/advance?roomId=1&requestedCheckIn=2024-02-01&requestedCheckOut=2024-02-05"
```

### Integration with Frontend
```javascript
const checkAdvanceAvailability = async (roomId, checkIn, checkOut) => {
  const response = await fetch(
    `/api/bookings/availability/advance?roomId=${roomId}&requestedCheckIn=${checkIn}&requestedCheckOut=${checkOut}`
  );
  const data = await response.json();
  return data.isAvailable;
};
```

## Conclusion

The advance booking implementation provides a robust, scalable solution for checking room availability for future dates. The design follows enterprise-grade patterns with proper separation of concerns, comprehensive error handling, and performance optimization.

Key benefits:
- **Clear Separation**: Distinguishes between immediate and advance availability
- **Performance Optimized**: Leverages existing database indexes
- **Business Rule Compliant**: Enforces future date requirements
- **Extensible**: Designed for future enhancements and scaling
- **Well Documented**: Comprehensive implementation guide for developers

This implementation serves as a foundation for more advanced booking features while maintaining the existing system's reliability and performance characteristics.
