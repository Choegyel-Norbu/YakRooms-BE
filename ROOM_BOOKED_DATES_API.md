# Room Booked Dates API

## Overview

Simple API endpoint to get all booked dates for a specific room. This enables frontend applications to block unavailable dates in date pickers and calendars. The method is implemented as part of the existing RoomService for better code organization.

## Endpoint

**GET** `/api/rooms/{roomId}/booked-dates`

**Description:** Get all booked dates for a specific room.

**Parameters:**
- `roomId` (path): The ID of the room

**Response:**
```json
{
  "roomId": 1,
  "roomNumber": "101",
  "bookedDates": [
    "2024-01-15",
    "2024-01-16", 
    "2024-01-17",
    "2024-01-25",
    "2024-01-26"
  ]
}
```

## Frontend Usage

### React Date Picker Example

```jsx
import React, { useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

const RoomBookingForm = ({ roomId }) => {
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  const [disabledDates, setDisabledDates] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchBookedDates();
  }, [roomId]);

  const fetchBookedDates = async () => {
    try {
      const response = await fetch(`/api/rooms/${roomId}/booked-dates`);
      const data = await response.json();
      
      // Convert string dates to Date objects
      const unavailableDates = data.bookedDates.map(dateStr => new Date(dateStr));
      setDisabledDates(unavailableDates);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching booked dates:', error);
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h3>Book Room {roomId}</h3>
      <div>
        <label>Check-in Date:</label>
        <DatePicker
          selected={startDate}
          onChange={date => setStartDate(date)}
          excludeDates={disabledDates}
          minDate={new Date()}
          placeholderText="Select check-in date"
        />
      </div>
      <div>
        <label>Check-out Date:</label>
        <DatePicker
          selected={endDate}
          onChange={date => setEndDate(date)}
          excludeDates={disabledDates}
          minDate={startDate || new Date()}
          placeholderText="Select check-out date"
        />
      </div>
    </div>
  );
};

export default RoomBookingForm;
```

### Vanilla JavaScript Example

```javascript
// Get booked dates for a room
async function getBookedDates(roomId) {
  try {
    const response = await fetch(`/api/rooms/${roomId}/booked-dates`);
    const data = await response.json();
    
    // Convert to Date objects for your date picker
    const bookedDates = data.bookedDates.map(dateStr => new Date(dateStr));
    
    // Use these dates to disable in your date picker
    console.log('Booked dates:', bookedDates);
    
    return bookedDates;
  } catch (error) {
    console.error('Error fetching booked dates:', error);
    return [];
  }
}

// Usage
const roomId = 1;
getBookedDates(roomId).then(bookedDates => {
  // Disable these dates in your date picker
  datePicker.disableDates(bookedDates);
});
```

## Testing

Use the provided test script:

```bash
chmod +x test_booked_dates.sh
./test_booked_dates.sh
```

## Notes

- **Public Access**: No authentication required
- **Performance**: Optimized query with proper indexing
- **Date Format**: ISO date format (YYYY-MM-DD)
- **Status Filtering**: Only includes PENDING, CONFIRMED, and CHECKED_IN bookings
- **Date Range**: Returns all dates from check-in to check-out (exclusive)
- **Duplicates**: Automatically removes duplicate dates and sorts them
