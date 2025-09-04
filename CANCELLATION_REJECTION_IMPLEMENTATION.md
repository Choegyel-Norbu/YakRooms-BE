# Booking Cancellation Rejection Implementation

## Overview

This document describes the implementation of the booking cancellation rejection functionality in the YakRooms system. The implementation ensures that when staff rejects a cancellation request, the booking remains active and room availability is **not** affected.

## Architecture

### Key Components

1. **BookingService Interface** - Defines the `rejectCancellationRequest()` method
2. **BookingServiceImpl** - Implements the rejection logic
3. **BookingController** - Exposes the REST endpoint
4. **NotificationService** - Handles guest notifications
5. **BookingValidationService** - Validates status transitions

### Status Flow

```
CANCELLATION_REQUESTED → CANCELLATION_REJECTED
```

## Implementation Details

### 1. Service Interface

```java
/**
 * Reject a booking cancellation request.
 * This method specifically handles cancellation rejection without affecting room availability.
 * 
 * @param bookingId The booking ID
 * @return true if the rejection was successful
 */
public boolean rejectCancellationRequest(Long bookingId);
```

### 2. Service Implementation

The `BookingServiceImpl.rejectCancellationRequest()` method:

1. **Validates the booking exists and is in correct status**
   - Only bookings with `CANCELLATION_REQUESTED` status can be rejected
   - Throws `BusinessException` if validation fails

2. **Updates booking status**
   - Changes status from `CANCELLATION_REQUESTED` to `CANCELLATION_REJECTED`
   - Persists the change to database

3. **Preserves room availability**
   - **CRITICAL**: Does NOT call `RoomAvailabilityService`
   - Room remains unavailable as the booking is still active
   - Logs this decision for audit purposes

4. **Creates guest notification**
   - Calls `NotificationService.createCancellationRejectionNotification()`
   - Notifies the guest that their cancellation request was rejected
   - Handles notification failures gracefully (non-blocking)

5. **Broadcasts WebSocket event**
   - Sends real-time update to all connected clients
   - Includes status transition details

### 3. REST Endpoint

```http
PUT /api/bookings/cancellation-requests/{bookingId}/reject?hotelId={hotelId}
```

**Authorization**: `HOTEL_ADMIN` or `STAFF` roles only

**Response Structure**:
```json
{
  "success": true,
  "message": "Cancellation request rejected successfully. The booking remains active and room availability is unchanged.",
  "bookingId": 123,
  "hotelId": 456,
  "newStatus": "CANCELLATION_REJECTED",
  "roomAvailabilityImpact": "none"
}
```

## Key Design Decisions

### 1. No Room Availability Changes

**Why**: When a cancellation request is rejected, the booking remains active. The room should continue to be unavailable for the original booking dates.

**Implementation**: The rejection method deliberately avoids calling any `RoomAvailabilityService` methods.

### 2. Dedicated Rejection Method

**Why**: Using the generic `updateBookingStatus()` method would trigger room availability updates through the status transition logic.

**Implementation**: Created a specialized `rejectCancellationRequest()` method that handles rejection-specific logic.

### 3. Comprehensive Error Handling

**Why**: Rejection can fail for various reasons (invalid status, booking not found, etc.).

**Implementation**: 
- Business exceptions for invalid operations
- System exceptions for technical failures
- Detailed error responses with error types

### 4. Non-blocking Notifications

**Why**: Notification failures shouldn't prevent the rejection from completing.

**Implementation**: Notification creation is wrapped in try-catch and logged but doesn't fail the operation.

## Validation Rules

### Status Transition Validation

The `BookingValidationService` defines valid transitions:

```java
// CANCELLATION_REQUESTED can transition to CANCELLED, CONFIRMED, or CANCELLATION_REJECTED
VALID_STATUS_TRANSITIONS.put(BookingStatus.CANCELLATION_REQUESTED, 
    Set.of(BookingStatus.CANCELLED, BookingStatus.CONFIRMED, BookingStatus.CANCELLATION_REJECTED));
```

### Business Rules

1. **Only `CANCELLATION_REQUESTED` bookings can be rejected**
2. **Rejected bookings cannot be cancelled again** (terminal state)
3. **Room availability remains unchanged after rejection**

## Testing

### Test Scenarios

1. **Happy Path**
   - Reject a booking with `CANCELLATION_REQUESTED` status
   - Verify status changes to `CANCELLATION_REJECTED`
   - Verify room availability unchanged
   - Verify notification created
   - Verify WebSocket event broadcast

2. **Error Scenarios**
   - Try to reject booking with wrong status
   - Try to reject non-existent booking
   - Try without proper authorization

3. **Edge Cases**
   - Notification service failure
   - WebSocket broadcasting failure
   - Database transaction rollback

### Test Script

Use the provided `test_cancellation_rejection.sh` script to test the endpoint:

```bash
./test_cancellation_rejection.sh
```

## Monitoring and Observability

### Logging

The implementation includes comprehensive logging:

- **Info Level**: Successful operations, status changes
- **Warn Level**: Invalid operations, business rule violations
- **Error Level**: System failures, notification failures

### Metrics

Key metrics to monitor:

- Rejection success rate
- Notification delivery rate
- WebSocket broadcast success rate
- Average rejection processing time

## Security Considerations

### Authorization

- Only `HOTEL_ADMIN` and `STAFF` can reject cancellations
- Uses Spring Security `@PreAuthorize` annotations
- Validates hotel ownership through booking details

### Input Validation

- Validates booking ID exists
- Validates booking status is correct
- Sanitizes error messages in responses

## Integration Points

### Dependencies

1. **NotificationService** - Guest notifications
2. **BookingWebSocketService** - Real-time updates
3. **BookingValidationService** - Business rule validation
4. **BookingRepository** - Data persistence

### External Systems

- **WebSocket Clients** - Real-time UI updates
- **Email/SMS Services** - Guest notifications (via NotificationService)
- **Audit Systems** - Status change tracking

## Future Enhancements

### Potential Improvements

1. **Rejection Reasons**
   - Allow staff to provide reason for rejection
   - Include reason in guest notification

2. **Automatic Rejection Rules**
   - Time-based automatic rejections
   - Policy-based rejection logic

3. **Rejection Analytics**
   - Track rejection patterns
   - Identify common rejection reasons

4. **Guest Communication**
   - Allow guests to respond to rejections
   - Provide alternative options

## Conclusion

The cancellation rejection implementation provides a robust, secure, and observable way for hotel staff to reject cancellation requests while maintaining data consistency and providing excellent user experience through real-time notifications.

The key architectural decision to **not** affect room availability ensures that rejected bookings remain active and rooms stay properly allocated, which is critical for hotel operations.
