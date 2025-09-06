#!/bin/bash

# Test script for cookie-based authentication
# This script tests the complete authentication flow with cookies

echo "=== Testing Cookie-Based Authentication ==="
echo

# Configuration
BASE_URL="http://localhost:8080"
COOKIE_FILE="cookies.txt"

# Clean up any existing cookie file
rm -f $COOKIE_FILE

echo "1. Testing login endpoint (this should set cookies)..."
echo "Note: You need to provide a valid Google ID token for this test"
echo

# Test login (you'll need to replace with actual Google ID token)
echo "Please provide a Google ID token for testing:"
read -p "Google ID Token: " GOOGLE_TOKEN

if [ -z "$GOOGLE_TOKEN" ]; then
    echo "No token provided. Using mock test..."
    echo "Testing with invalid token to see error response..."
    GOOGLE_TOKEN="invalid-token"
fi

echo
echo "Sending login request..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/firebase" \
  -H "Content-Type: application/json" \
  -d "{\"idToken\":\"$GOOGLE_TOKEN\"}" \
  -c $COOKIE_FILE \
  -w "HTTP_STATUS:%{http_code}")

echo "Login Response:"
echo "$LOGIN_RESPONSE" | sed 's/HTTP_STATUS:.*//'
echo

# Extract HTTP status
HTTP_STATUS=$(echo "$LOGIN_RESPONSE" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
echo "HTTP Status: $HTTP_STATUS"
echo

# Check if cookies were set
echo "2. Checking if cookies were set..."
if [ -f "$COOKIE_FILE" ] && [ -s "$COOKIE_FILE" ]; then
    echo "Cookies found:"
    cat $COOKIE_FILE
    echo
else
    echo "No cookies found in response!"
    echo
fi

# Test protected endpoint
echo "3. Testing protected endpoint with cookies..."
PROTECTED_RESPONSE=$(curl -s -X GET "$BASE_URL/api/bookings" \
  -b $COOKIE_FILE \
  -w "HTTP_STATUS:%{http_code}")

echo "Protected endpoint response:"
echo "$PROTECTED_RESPONSE" | sed 's/HTTP_STATUS:.*//'
echo

# Extract HTTP status for protected endpoint
PROTECTED_STATUS=$(echo "$PROTECTED_RESPONSE" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
echo "Protected endpoint HTTP Status: $PROTECTED_STATUS"
echo

# Test refresh token endpoint
echo "4. Testing refresh token endpoint..."
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/refresh-token" \
  -b $COOKIE_FILE \
  -w "HTTP_STATUS:%{http_code}")

echo "Refresh token response:"
echo "$REFRESH_RESPONSE" | sed 's/HTTP_STATUS:.*//'
echo

# Extract HTTP status for refresh
REFRESH_STATUS=$(echo "$REFRESH_RESPONSE" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
echo "Refresh token HTTP Status: $REFRESH_STATUS"
echo

# Test logout
echo "5. Testing logout endpoint..."
LOGOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/logout" \
  -b $COOKIE_FILE \
  -w "HTTP_STATUS:%{http_code}")

echo "Logout response:"
echo "$LOGOUT_RESPONSE" | sed 's/HTTP_STATUS:.*//'
echo

# Extract HTTP status for logout
LOGOUT_STATUS=$(echo "$LOGOUT_RESPONSE" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
echo "Logout HTTP Status: $LOGOUT_STATUS"
echo

# Summary
echo "=== Test Summary ==="
echo "Login Status: $HTTP_STATUS"
echo "Protected Endpoint Status: $PROTECTED_STATUS"
echo "Refresh Token Status: $REFRESH_STATUS"
echo "Logout Status: $LOGOUT_STATUS"
echo

if [ "$HTTP_STATUS" = "200" ] && [ "$PROTECTED_STATUS" = "200" ]; then
    echo "✅ Authentication flow working correctly!"
else
    echo "❌ Authentication flow has issues"
    echo "Check the debug output in the application logs"
fi

# Clean up
rm -f $COOKIE_FILE
echo
echo "Test completed. Cookie file cleaned up."
