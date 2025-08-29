# Advance Booking with DTO - Usage Examples

## Overview

The advance booking functionality now supports two endpoints:
1. **GET** `/api/bookings/availability/advance` - Query parameters (original)
2. **POST** `/api/bookings/availability/advance` - JSON DTO (new)

## New DTO-Based Endpoint

### Endpoint Details
- **URL**: `POST /api/bookings/availability/advance`
- **Content-Type**: `application/json`
- **Access**: Public (no authentication required)
- **Method**: POST with JSON body

### Request Structure

#### Required Fields
```json
{
  "roomId": 1,
  "checkInDate": "2024-02-01",
  "checkOutDate": "2024-02-05"
}
```

#### Optional Fields
```json
{
  "roomId": 1,
  "hotelId": 1,
  "checkInDate": "2024-02-01",
  "checkOutDate": "2024-02-05",
  "guests": 2,
  "numberOfRooms": 1,
  "phone": "+1234567890",
  "cid": "ABC123",
  "destination": "New York",
  "origin": "Los Angeles"
}
```

## Usage Examples

### 1. Basic Availability Check

#### cURL
```bash
curl -X POST "http://localhost:8080/api/bookings/availability/advance" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "checkInDate": "2024-02-01",
    "checkOutDate": "2024-02-05"
  }'
```

#### JavaScript/Frontend
```javascript
const checkAvailability = async () => {
  const requestData = {
    roomId: 1,
    checkInDate: "2024-02-01",
    checkOutDate: "2024-02-05"
  };

  try {
    const response = await fetch('/api/bookings/availability/advance', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestData)
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('Availability:', result.isAvailable);
      console.log('Message:', result.message);
    } else {
      console.error('Error:', result.error);
    }
  } catch (error) {
    console.error('Request failed:', error);
  }
};
```

### 2. Advanced Availability Check with Additional Details

#### cURL
```bash
curl -X POST "http://localhost:8080/api/bookings/availability/advance" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "hotelId": 1,
    "checkInDate": "2024-02-01",
    "checkOutDate": "2024-02-05",
    "guests": 4,
    "numberOfRooms": 2,
    "phone": "+1234567890",
    "destination": "New York",
    "origin": "Los Angeles"
  }'
```

#### JavaScript/Frontend
```javascript
const checkAdvancedAvailability = async (bookingDetails) => {
  const requestData = {
    roomId: bookingDetails.roomId,
    hotelId: bookingDetails.hotelId,
    checkInDate: bookingDetails.checkInDate,
    checkOutDate: bookingDetails.checkOutDate,
    guests: bookingDetails.guests,
    numberOfRooms: bookingDetails.numberOfRooms,
    phone: bookingDetails.phone,
    destination: bookingDetails.destination,
    origin: bookingDetails.origin
  };

  try {
    const response = await fetch('/api/bookings/availability/advance', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestData)
    });

    const result = await response.json();
    
    if (response.ok) {
      return {
        success: true,
        isAvailable: result.isAvailable,
        message: result.message,
        details: result
      };
    } else {
      return {
        success: false,
        error: result.error
      };
    }
  } catch (error) {
    return {
      success: false,
      error: 'Request failed: ' + error.message
    };
  }
};

// Usage
const bookingDetails = {
  roomId: 1,
  hotelId: 1,
  checkInDate: "2024-02-01",
  checkOutDate: "2024-02-05",
  guests: 4,
  numberOfRooms: 2,
  phone: "+1234567890",
  destination: "New York",
  origin: "Los Angeles"
};

const result = await checkAdvancedAvailability(bookingDetails);
if (result.success) {
  console.log('Room available:', result.isAvailable);
  console.log('Details:', result.details);
} else {
  console.error('Error:', result.error);
}
```

### 3. React Component Example

```jsx
import React, { useState } from 'react';

const AvailabilityChecker = () => {
  const [formData, setFormData] = useState({
    roomId: '',
    checkInDate: '',
    checkOutDate: '',
    guests: 1,
    numberOfRooms: 1
  });
  
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('/api/bookings/availability/advance', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData)
      });

      const data = await response.json();
      setResult(data);
    } catch (error) {
      setResult({ error: 'Request failed: ' + error.message });
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <div className="availability-checker">
      <h2>Check Room Availability</h2>
      
      <form onSubmit={handleSubmit}>
        <div>
          <label>Room ID:</label>
          <input
            type="number"
            name="roomId"
            value={formData.roomId}
            onChange={handleInputChange}
            required
          />
        </div>
        
        <div>
          <label>Check-in Date:</label>
          <input
            type="date"
            name="checkInDate"
            value={formData.checkInDate}
            onChange={handleInputChange}
            required
          />
        </div>
        
        <div>
          <label>Check-out Date:</label>
          <input
            type="date"
            name="checkOutDate"
            value={formData.checkOutDate}
            onChange={handleInputChange}
            required
          />
        </div>
        
        <div>
          <label>Guests:</label>
          <input
            type="number"
            name="guests"
            value={formData.guests}
            onChange={handleInputChange}
            min="1"
            max="20"
          />
        </div>
        
        <div>
          <label>Number of Rooms:</label>
          <input
            type="number"
            name="numberOfRooms"
            value={formData.numberOfRooms}
            onChange={handleInputChange}
            min="1"
            max="10"
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Checking...' : 'Check Availability'}
        </button>
      </form>

      {result && (
        <div className="result">
          {result.error ? (
            <div className="error">
              <h3>Error</h3>
              <p>{result.error}</p>
            </div>
          ) : (
            <div className="success">
              <h3>Availability Result</h3>
              <p><strong>Room ID:</strong> {result.roomId}</p>
              <p><strong>Check-in:</strong> {result.requestedCheckIn}</p>
              <p><strong>Check-out:</strong> {result.requestedCheckOut}</p>
              <p><strong>Available:</strong> {result.isAvailable ? 'Yes' : 'No'}</p>
              <p><strong>Message:</strong> {result.message}</p>
              {result.hotelId && <p><strong>Hotel ID:</strong> {result.hotelId}</p>}
              {result.guests && <p><strong>Guests:</strong> {result.guests}</p>}
              {result.numberOfRooms && <p><strong>Number of Rooms:</strong> {result.numberOfRooms}</p>}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AvailabilityChecker;
```

### 4. Python Example

```python
import requests
import json
from datetime import date, timedelta

def check_room_availability(room_id, check_in_date, check_out_date, **kwargs):
    """
    Check room availability using the DTO endpoint
    
    Args:
        room_id (int): Room ID to check
        check_in_date (str): Check-in date in YYYY-MM-DD format
        check_out_date (str): Check-out date in YYYY-MM-DD format
        **kwargs: Additional optional parameters
    
    Returns:
        dict: Response from the API
    """
    url = "http://localhost:8080/api/bookings/availability/advance"
    
    # Build request payload
    payload = {
        "roomId": room_id,
        "checkInDate": check_in_date,
        "checkOutDate": check_out_date
    }
    
    # Add optional parameters
    payload.update(kwargs)
    
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        return {"error": f"Request failed: {str(e)}"}

# Example usage
if __name__ == "__main__":
    # Basic check
    result = check_room_availability(
        room_id=1,
        check_in_date="2024-02-01",
        check_out_date="2024-02-05"
    )
    print("Basic check result:", json.dumps(result, indent=2))
    
    # Advanced check with additional details
    result = check_room_availability(
        room_id=1,
        check_in_date="2024-02-01",
        check_out_date="2024-02-05",
        hotel_id=1,
        guests=4,
        numberOf_rooms=2,
        destination="New York",
        origin="Los Angeles"
    )
    print("Advanced check result:", json.dumps(result, indent=2))
```

## Response Examples

### Success Response
```json
{
  "roomId": 1,
  "requestedCheckIn": "2024-02-01",
  "requestedCheckOut": "2024-02-05",
  "isAvailable": true,
  "message": "Room is available for advance booking",
  "hotelId": 1,
  "guests": 4,
  "numberOfRooms": 2
}
```

### Error Response (Missing Required Field)
```json
{
  "error": "Room ID is required"
}
```

### Error Response (Business Logic Error)
```json
{
  "error": "Check-in date cannot be in the past for advance bookings",
  "roomId": 1,
  "requestedCheckIn": "2023-01-01",
  "requestedCheckOut": "2023-01-05"
}
```

## Key Benefits of DTO Approach

### 1. **Structured Data**
- Consistent request format
- Easy to validate and process
- Better error handling

### 2. **Extensibility**
- Easy to add new fields
- Backward compatible
- Supports complex booking scenarios

### 3. **Frontend Integration**
- Better form handling
- JSON-based communication
- Easier to implement validation

### 4. **API Consistency**
- Follows REST best practices
- Consistent with other endpoints
- Better documentation and testing

## Testing

Use the provided test script to verify the functionality:

```bash
./test_advance_booking_dto.sh
```

This will test various scenarios including:
- Valid requests with different configurations
- Missing required fields
- Comparison with the GET endpoint
- Error handling

## Migration Guide

### From Query Parameters to DTO

#### Before (GET endpoint)
```javascript
// Old way - query parameters
const response = await fetch(
  `/api/bookings/availability/advance?roomId=1&requestedCheckIn=2024-02-01&requestedCheckOut=2024-02-05`
);
```

#### After (POST endpoint)
```javascript
// New way - JSON DTO
const response = await fetch('/api/bookings/availability/advance', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    roomId: 1,
    checkInDate: "2024-02-01",
    checkOutDate: "2024-02-05"
  })
});
```

### Backward Compatibility

Both endpoints are available, so you can:
1. **Gradually migrate** from GET to POST
2. **Keep both** for different use cases
3. **Use GET** for simple checks, **POST** for complex scenarios

The DTO-based approach provides a more robust and extensible solution while maintaining backward compatibility.
