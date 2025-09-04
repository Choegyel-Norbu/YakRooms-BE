#!/bin/bash

# Test script for search bookings by room number functionality
# This script tests the new search endpoint with various scenarios

BASE_URL="http://localhost:8080/api/bookings"
HOTEL_ID="1"  # Replace with actual hotel ID from your database

echo "=== Testing Search Bookings by Room Number ==="
echo "Base URL: $BASE_URL"
echo "Hotel ID: $HOTEL_ID"
echo ""

# Test 1: Search with valid room number
echo "Test 1: Search with valid room number '101'"
curl -X GET "$BASE_URL/search/room-number?roomNumber=101&hotelId=$HOTEL_ID&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "----------------------------------------"
echo ""

# Test 2: Search with different room number
echo "Test 2: Search with room number '201'"
curl -X GET "$BASE_URL/search/room-number?roomNumber=201&hotelId=$HOTEL_ID&page=0&size=5" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "----------------------------------------"
echo ""

# Test 3: Search with case-insensitive room number
echo "Test 3: Search with lowercase room number '101' (should be normalized to uppercase)"
curl -X GET "$BASE_URL/search/room-number?roomNumber=101&hotelId=$HOTEL_ID&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "----------------------------------------"
echo ""

# Test 4: Search with room number that has spaces (should be trimmed)
echo "Test 4: Search with room number ' 101 ' (should be trimmed)"
curl -X GET "$BASE_URL/search/room-number?roomNumber=%20101%20&hotelId=$HOTEL_ID&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "----------------------------------------"
echo ""

# Test 5: Search with non-existent room number
echo "Test 5: Search with non-existent room number '999'"
curl -X GET "$BASE_URL/search/room-number?roomNumber=999&hotelId=$HOTEL_ID&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "----------------------------------------"
echo ""

# Test 6: Search with empty room number (should return 400)
echo "Test 6: Search with empty room number (should return 400)"
curl -X GET "$BASE_URL/search/room-number?roomNumber=&hotelId=$HOTEL_ID&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "----------------------------------------"
echo ""

# Test 7: Search without hotel ID (should return 400)
echo "Test 7: Search without hotel ID (should return 400)"
curl -X GET "$BASE_URL/search/room-number?roomNumber=101&page=0&size=10" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || echo "Response received (jq not available for formatting)"

echo ""
echo "=== Test Summary ==="
echo "1. Valid room number search"
echo "2. Different room number search"
echo "3. Case-insensitive search"
echo "4. Trimmed room number search"
echo "5. Non-existent room number search"
echo "6. Empty room number validation"
echo "7. Missing hotel ID validation"
echo ""
echo "Note: Replace YOUR_JWT_TOKEN_HERE with actual JWT token for authentication"
echo "Note: Replace HOTEL_ID with actual hotel ID from your database"
