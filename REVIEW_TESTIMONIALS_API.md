# Review Testimonials API Documentation

This document describes the API endpoints for fetching hotel reviews for testimonials.

## Endpoints

### 1. Get All Reviews for a Hotel (Testimonials)

**Endpoint:** `GET /api/reviews/hotel/{hotelId}/testimonials`

**Description:** Retrieves all reviews for a specific hotel, ordered by creation date (newest first). This endpoint is designed for displaying testimonials.

**Path Parameters:**
- `hotelId` (Long): The ID of the hotel

**Response:**
```json
[
  {
    "id": 1,
    "rating": 5,
    "comment": "Excellent hotel with great service!",
    "userId": 123,
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "userProfilePicUrl": "https://example.com/profile.jpg",
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "rating": 4,
    "comment": "Good experience, would recommend",
    "userId": 456,
    "userName": "Jane Smith",
    "userEmail": "jane.smith@example.com",
    "userProfilePicUrl": "https://example.com/profile2.jpg",
    "createdAt": "2024-01-14T15:45:00"
  }
]
```

**HTTP Status Codes:**
- `200 OK`: Reviews retrieved successfully
- `404 Not Found`: Hotel not found
- `500 Internal Server Error`: Server error

### 2. Get Paginated Reviews for a Hotel (Testimonials with Pagination)

**Endpoint:** `GET /api/reviews/hotel/{hotelId}/testimonials/paginated`

**Description:** Retrieves paginated reviews for a specific hotel, ordered by creation date (newest first). This endpoint is useful when there are many reviews and you want to implement pagination.

**Path Parameters:**
- `hotelId` (Long): The ID of the hotel

**Query Parameters:**
- `page` (int, optional): Page number (default: 0)
- `size` (int, optional): Number of reviews per page (default: 10)

**Example Request:**
```
GET /api/reviews/hotel/1/testimonials/paginated?page=0&size=5
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "rating": 5,
      "comment": "Excellent hotel with great service!",
      "userId": 123,
      "userName": "John Doe",
      "userEmail": "john.doe@example.com",
      "userProfilePicUrl": "https://example.com/profile.jpg",
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 5,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 25,
  "totalPages": 5,
  "last": false,
  "first": true,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 5,
  "size": 5,
  "number": 0,
  "empty": false
}
```

**HTTP Status Codes:**
- `200 OK`: Reviews retrieved successfully
- `404 Not Found`: Hotel not found
- `500 Internal Server Error`: Server error

## Implementation Details

### Repository Layer
- `ReviewRepository.findAllByHotelIdOrderByCreatedAtDesc(Long hotelId)`: Returns all reviews for a hotel
- `ReviewRepository.findAllByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable)`: Returns paginated reviews for a hotel

### Service Layer
- `ReviewService.getAllReviewsForHotel(Long hotelId)`: Business logic for retrieving all reviews
- `ReviewService.getReviewsForHotelPaginated(Long hotelId, Pageable pageable)`: Business logic for retrieving paginated reviews

### Controller Layer
- `ReviewController.getAllReviewsForHotel(Long hotelId)`: Handles the testimonials endpoint
- `ReviewController.getReviewsForHotelPaginated(Long hotelId, int page, int size)`: Handles the paginated testimonials endpoint

## Error Handling

The API includes comprehensive error handling:
- **ResourceNotFoundException**: When the specified hotel doesn't exist
- **IllegalArgumentException**: For invalid input parameters
- **General Exception**: For unexpected server errors

## Performance Considerations

1. **Indexing**: The reviews table has indexes on `(hotel_id, created_at DESC)` for optimal query performance
2. **Pagination**: Use the paginated endpoint for hotels with many reviews to avoid loading all data at once
3. **Lazy Loading**: User information is fetched efficiently using JPA relationships

## Usage Examples

### Frontend Integration

```javascript
// Fetch all testimonials for a hotel
fetch('/api/reviews/hotel/1/testimonials')
  .then(response => response.json())
  .then(testimonials => {
    testimonials.forEach(testimonial => {
      console.log(`${testimonial.userName}: ${testimonial.comment}`);
    });
  });

// Fetch paginated testimonials
fetch('/api/reviews/hotel/1/testimonials/paginated?page=0&size=5')
  .then(response => response.json())
  .then(data => {
    data.content.forEach(testimonial => {
      console.log(`${testimonial.userName}: ${testimonial.comment}`);
    });
    console.log(`Total pages: ${data.totalPages}`);
  });
```

### React Component Example

```jsx
import React, { useState, useEffect } from 'react';

const Testimonials = ({ hotelId }) => {
  const [testimonials, setTestimonials] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`/api/reviews/hotel/${hotelId}/testimonials`)
      .then(response => response.json())
      .then(data => {
        setTestimonials(data);
        setLoading(false);
      });
  }, [hotelId]);

  if (loading) return <div>Loading testimonials...</div>;

  return (
    <div className="testimonials">
      <h3>Customer Reviews</h3>
      {testimonials.map(testimonial => (
        <div key={testimonial.id} className="testimonial">
          <div className="user-info">
            <img src={testimonial.userProfilePicUrl} alt={testimonial.userName} />
            <span>{testimonial.userName}</span>
          </div>
          <div className="rating">{'★'.repeat(testimonial.rating)}</div>
          <p>{testimonial.comment}</p>
          <small>{new Date(testimonial.createdAt).toLocaleDateString()}</small>
        </div>
      ))}
    </div>
  );
};

export default Testimonials;
``` 