# WebSocket SockJS Path Issue - Resolution

## Problem Description

The application was experiencing the following error:
```
Invalid SockJS path '/bookings' - required to have 3 path segments
```

This error occurred because the client was trying to connect to `/ws/bookings`, but SockJS has specific path requirements for its internal routing.

## Root Cause

When using SockJS with Spring WebSocket, the client should connect to the base endpoint (e.g., `/ws`) rather than a specific sub-path (e.g., `/ws/bookings`). SockJS automatically handles the internal routing and requires specific path patterns.

**Incorrect client connection:**
```javascript
const socket = new SockJS('/ws/bookings');  // ❌ Causes "Invalid SockJS path" error
```

**Correct client connection:**
```javascript
const socket = new SockJS('/ws');  // ✅ Correct - connects to base endpoint
```

## Solution Implemented

### 1. Updated WebSocket Configuration

Modified `src/main/java/com/yakrooms/be/config/WebSocketConfig.java`:
- Added both `/ws` and `/ws/bookings` endpoints for flexibility
- Added comprehensive logging for debugging
- Maintained SockJS support on both endpoints

### 2. Updated Documentation

Modified `src/main/java/com/yakrooms/be/docs/WebSocketBookingEvents.md`:
- Added clear explanation of SockJS path requirements
- Updated code examples to show correct connection pattern
- Added warnings about incorrect connection patterns

### 3. Created Test Page

Added `src/main/resources/static/websocket-test.html`:
- Demonstrates correct vs. incorrect connection patterns
- Shows the "Invalid SockJS path" error when connecting to `/ws/bookings`
- Provides interactive testing of WebSocket functionality

### 4. Enhanced Test Controller

Updated `src/main/java/com/yakrooms/be/controller/WebSocketTestController.java`:
- Added message handler for test messages
- Enhanced logging for debugging

## How to Use

### For Frontend Developers

1. **Connect to WebSocket:**
   ```javascript
   // ✅ CORRECT - Connect to base endpoint
   const socket = new SockJS('/ws');
   const stompClient = Stomp.over(socket);
   ```

2. **Subscribe to Topics:**
   ```javascript
   // After connecting, subscribe to specific topics
   stompClient.subscribe('/topic/bookings', function(message) {
       console.log('Booking event:', message.body);
   });
   
   stompClient.subscribe('/topic/hotels/123/bookings', function(message) {
       console.log('Hotel-specific booking event:', message.body);
   });
   ```

3. **Send Messages:**
   ```javascript
   // Send messages to application destinations
   stompClient.send("/app/bookings/status", {}, JSON.stringify(data));
   ```

### Testing the Fix

1. **Start the application**
2. **Navigate to:** `http://localhost:8080/websocket-test.html`
3. **Test correct connection:** Click "Connect to /ws" - should succeed
4. **Test incorrect connection:** Click "Connect to /ws/bookings" - should show the SockJS path error
5. **Test messaging:** Use "Send Test Message" button to test bidirectional communication

## Available WebSocket Endpoints

- **`/ws`** - Primary endpoint for all WebSocket connections
- **`/ws/bookings`** - Alternative endpoint (if needed)

## Available Topics and Queues

- **`/topic/bookings`** - General booking events
- **`/topic/hotels/{hotelId}/bookings`** - Hotel-specific booking events
- **`/queue/users/{userId}/bookings`** - User-specific booking events
- **`/topic/test`** - Test messages

## Important Notes

1. **SockJS Path Requirements:** SockJS automatically adds path segments for internal routing
2. **Connection Pattern:** Always connect to the base endpoint, then subscribe to specific topics
3. **Error Handling:** The "Invalid SockJS path" error is now clearly documented and avoidable
4. **Testing:** Use the provided test page to verify WebSocket functionality

## Logging

The WebSocket configuration now includes comprehensive logging:
- Connection attempts and successes
- Endpoint registration
- Message broker configuration
- Message converter setup

Check the application logs for WebSocket-related information.

## Production Considerations

1. **Security:** Restrict allowed origins in production
2. **Authentication:** Implement WebSocket authentication
3. **Monitoring:** Monitor WebSocket connection counts and performance
4. **Scaling:** Consider clustering for high-traffic scenarios

## Related Files

- `src/main/java/com/yakrooms/be/config/WebSocketConfig.java`
- `src/main/java/com/yakrooms/be/docs/WebSocketBookingEvents.md`
- `src/main/resources/static/websocket-test.html`
- `src/main/java/com/yakrooms/be/controller/WebSocketTestController.java`
