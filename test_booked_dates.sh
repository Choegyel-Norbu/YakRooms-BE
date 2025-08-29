#!/bin/bash

# Test script for Room Booked Dates API endpoint
# This script demonstrates how to get booked dates for a room
# The method is now part of RoomService instead of a separate service

BASE_URL="http://localhost:8080/api/rooms"
ROOM_ID="1"  # Replace with actual room ID from your system

echo "=== Room Booked Dates API Test ==="
echo "Base URL: $BASE_URL"
echo "Room ID: $ROOM_ID"
echo "Note: Method is now part of RoomService"
echo ""

# Test: Get booked dates for a room
echo "Getting booked dates for room $ROOM_ID..."
curl -s -X GET "$BASE_URL/$ROOM_ID/booked-dates" \
  -H "Content-Type: application/json" | jq '.'
echo ""

echo "=== Test Complete ==="
echo ""
echo "Frontend Integration:"
echo "- Use this endpoint to get all booked dates for a room"
echo "- The response includes 'bookedDates' array with dates to block"
echo "- Block these dates in your date picker/calendar components"
echo "- Method is now part of RoomService for better organization"
echo ""
echo "Example Response:"
echo '{
  "roomId": 1,
  "roomNumber": "101",
  "bookedDates": ["2024-01-15", "2024-01-16", "2024-01-17"]
}'
