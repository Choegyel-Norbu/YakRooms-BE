#!/bin/bash

# Test script for the new search bookings feature
echo "Testing Search Bookings Feature"
echo "================================"

# Base URL for the API
BASE_URL="http://localhost:8080/api/bookings"

# Test 1: Search by CID
echo "Test 1: Searching by CID"
curl -X POST "$BASE_URL/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"cid": "1090904920490"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

# Test 2: Search by Phone
echo "Test 2: Searching by Phone"
curl -X POST "$BASE_URL/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"phone": "1483095"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

# Test 3: Search by Booking Number (Passcode)
echo "Test 3: Searching by Booking Number"
curl -X POST "$BASE_URL/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"number": "123456"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

# Test 4: Search by Check-in Date
echo "Test 4: Searching by Check-in Date"
curl -X POST "$BASE_URL/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"checkIn": "2024-01-15"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

# Test 5: Search within a specific hotel
echo "Test 5: Searching within Hotel ID 1"
curl -X POST "$BASE_URL/hotel/1/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"cid": "1090904920490"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

# Test 6: Invalid request (multiple fields)
echo "Test 6: Invalid request with multiple fields (should fail)"
curl -X POST "$BASE_URL/search" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"cid": "1090904920490", "phone": "1483095"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "Search tests completed!"
echo "Note: Replace 'YOUR_JWT_TOKEN' with an actual JWT token for HOTEL_ADMIN or STAFF role"
