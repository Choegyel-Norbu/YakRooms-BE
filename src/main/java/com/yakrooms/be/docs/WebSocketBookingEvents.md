# WebSocket Booking Events Documentation

## Overview
The WebSocket booking events system provides real-time notifications when booking statuses change, particularly when guests check out. This system enables frontend applications to receive instant updates without polling the server.

## Architecture

### Components
1. **BookingChangeEvent** - DTO containing booking change information
2. **BookingWebSocketService** - Service for broadcasting events
3. **BookingWebSocketController** - Controller for testing and managing events
4. **WebSocket Configuration** - STOMP over WebSocket setup

### Event Flow
1. Booking status changes in `BookingServiceImpl.updateBookingStatus()`
2. `broadcastBookingStatusChange()` method creates and sends event
3. WebSocket service broadcasts to multiple topics/queues
4. Frontend receives real-time updates

## WebSocket Connection

### Important: SockJS Path Requirements
When using SockJS, the client should connect to the base endpoint `/ws`, not `/ws/bookings`. SockJS automatically handles the internal routing and requires specific path patterns.

**Correct connection:**
```javascript
const socket = new SockJS('/ws');  // ✅ Correct
```

**Incorrect connection:**
```javascript
const socket = new SockJS('/ws/bookings');  // ❌ Will cause "Invalid SockJS path" error
```

### Available Endpoints
- **Primary**: `/ws` - Main WebSocket endpoint for all connections
- **Alternative**: `/ws/bookings` - Specific endpoint for booking-related connections (if needed)

## WebSocket Topics and Queues

### General Booking Events
- **Topic**: `/topic/bookings`
- **Description**: All booking status changes
- **Use Case**: Dashboard, admin panels, general monitoring

### Hotel-Specific Events
- **Topic**: `/topic/hotels/{hotelId}/bookings`
- **Description**: Booking events for a specific hotel
- **Use Case**: Hotel management dashboard, staff notifications

### User-Specific Events
- **Queue**: `/queue/users/{userId}/bookings`
- **Description**: Booking events for a specific user
- **Use Case**: User dashboard, personal notifications

## Event Structure

### BookingChangeEvent
```json
{
  "bookingId": 123,
  "hotelId": 456,
  "userId": 789,
  "oldStatus": "CHECKED_IN",
  "newStatus": "CHECKED_OUT",
  "eventType": "BOOKING_STATUS_CHANGE",
  "timestamp": "2024-01-15T10:30:00",
  "message": "Booking status changed from CHECKED_IN to CHECKED_OUT"
}
```

### Status Values
- `PENDING` - Booking created, awaiting confirmation
- `CONFIRMED` - Booking confirmed by hotel
- `CHECKED_IN` - Guest has checked in
- `CHECKED_OUT` - Guest has checked out
- `CANCELLED` - Booking cancelled

## Frontend Integration

### JavaScript/TypeScript Example
```javascript
// Connect to WebSocket - IMPORTANT: Use /ws, not /ws/bookings
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected to WebSocket');
    
    // Subscribe to general booking events
    stompClient.subscribe('/topic/bookings', function (message) {
        const event = JSON.parse(message.body);
        console.log('Booking event received:', event);
        handleBookingEvent(event);
    });
    
    // Subscribe to hotel-specific events
    const hotelId = 123;
    stompClient.subscribe(`/topic/hotels/${hotelId}/bookings`, function (message) {
        const event = JSON.parse(message.body);
        console.log('Hotel booking event:', event);
        handleHotelBookingEvent(event);
    });
    
    // Subscribe to user-specific events
    const userId = 456;
    stompClient.subscribe(`/queue/users/${userId}/bookings`, function (message) {
        const event = JSON.parse(message.body);
        console.log('User booking event:', event);
        handleUserBookingEvent(event);
    });
});

function handleBookingEvent(event) {
    switch (event.newStatus) {
        case 'CHECKED_OUT':
            showCheckoutNotification(event);
            updateRoomAvailability(event.hotelId);
            break;
        case 'CHECKED_IN':
            showCheckinNotification(event);
            break;
        case 'CANCELLED':
            showCancellationNotification(event);
            break;
    }
}

function showCheckoutNotification(event) {
    // Show notification to hotel staff
    const notification = {
        title: 'Guest Checked Out',
        message: `Guest has checked out from booking #${event.bookingId}`,
        type: 'success'
    };
    showNotification(notification);
}
```

### React Hook Example
```typescript
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

interface BookingChangeEvent {
  bookingId: number;
  hotelId: number;
  userId: number;
  oldStatus: string;
  newStatus: string;
  eventType: string;
  timestamp: string;
  message: string;
}

export const useBookingWebSocket = (hotelId?: number, userId?: number) => {
  const [events, setEvents] = useState<BookingChangeEvent[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    // IMPORTANT: Connect to /ws, not /ws/bookings
    const socket = new SockJS('/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setConnected(true);
        console.log('Connected to booking WebSocket');
        
        // Subscribe to general events
        stompClient.subscribe('/topic/bookings', (message) => {
          const event = JSON.parse(message.body);
          setEvents(prev => [...prev, event]);
        });
        
        // Subscribe to hotel-specific events
        if (hotelId) {
          stompClient.subscribe(`/topic/hotels/${hotelId}/bookings`, (message) => {
            const event = JSON.parse(message.body);
            setEvents(prev => [...prev, event]);
          });
        }
        
        // Subscribe to user-specific events
        if (userId) {
          stompClient.subscribe(`/queue/users/${userId}/bookings`, (message) => {
            const event = JSON.parse(message.body);
            setEvents(prev => [...prev, event]);
          });
        }
      },
      onDisconnect: () => {
        setConnected(false);
        console.log('Disconnected from booking WebSocket');
      }
    });
    
    stompClient.activate();
    
    return () => {
      stompClient.deactivate();
    };
  }, [hotelId, userId]);

  return { events, connected };
};
```

## Testing Endpoints

### Test Booking Status Change
```http
POST /api/websocket/bookings/test/status-change
Content-Type: application/x-www-form-urlencoded

bookingId=123&hotelId=456&userId=789&oldStatus=CHECKED_IN&newStatus=CHECKED_OUT
```

### Test Check-out Event
```http
POST /api/websocket/bookings/test/checkout
Content-Type: application/x-www-form-urlencoded

bookingId=123&hotelId=456&userId=789
```

### Get WebSocket Status
```http
GET /api/websocket/bookings/status
```

## Implementation Details

### When Events Are Triggered
Events are automatically triggered when:
1. `BookingController.updateBookingStatus()` is called
2. Booking status changes to any new value
3. Particularly important for `CHECKED_OUT` status

### Error Handling
- WebSocket broadcasting failures are logged but don't affect the main booking flow
- Frontend should handle connection failures gracefully
- Implement reconnection logic for production use

### Security Considerations
- WebSocket connections should be authenticated in production
- Consider implementing user-specific subscriptions based on roles
- Validate hotel and user access permissions

## Production Deployment

### Configuration
```properties
# WebSocket configuration
spring.websocket.max-text-message-size=8192
spring.websocket.max-binary-message-size=8192
```

### Monitoring
- Monitor WebSocket connection counts
- Log event broadcasting success/failure rates
- Set up alerts for WebSocket service failures

### Scaling Considerations
- WebSocket connections are stateful - consider clustering
- Use Redis or similar for session management in multi-instance deployments
- Implement connection pooling for high-traffic scenarios 