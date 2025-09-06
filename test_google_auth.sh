#!/bin/bash

# Test Google Token Authentication for YakRooms
# This script tests the complete authentication flow using the provided Google token

# Configuration
BASE_URL="http://localhost:8080"
GOOGLE_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6ImVmMjQ4ZjQyZjc0YWUwZjk0OTIwYWY5YTlhMDEzMTdlZjJkMzVmZTEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQ2hvZWd5ZWwgTm9yYnUiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jSjI3NVRfdVZEUVIwUjdtS2JxRUxiZEFOTFlaWnhRWi1kTUFfX3lHZFdBcW5rOEV3PXM5Ni1jIiwiaXNzIjoiaHR0cHM6Ly9zZWN1cmV0b2tlbi5nb29nbGUuY29tL3lha3Jvb21zIiwiYXVkIjoieWFrcm9vbXMiLCJhdXRoX3RpbWUiOjE3NTcwNzA5NDcsInVzZXJfaWQiOiJzTkVreXlLVmpsUXkzNkxWa0NVSzhzdG1FYzgzIiwic3ViIjoic05Fa3l5S1ZqbFF5MzZMVmtDVUs4c3RtRWM4MyIsImlhdCI6MTc1NzA3MDk0NywiZXhwIjoxNzU3MDc0NTQ3LCJlbWFpbCI6ImNob2VneWVsbEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJnb29nbGUuY29tIjpbIjEwODMyNjg5NzY3NzA0NDI3MDcwNCJdLCJlbWFpbCI6WyJjaG9lZ3llbGxAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoiZ29vZ2xlLmNvbSJ9fQ.RBO-mf43zi_DtGmY3YMA8-p8cEx69xhhTGHFSX6qwoRtMBGESP3P-fc21zVCU-Vt8qn1X61u3VdxO68whkW3cFN5TSq2xNNis9P7GPDml2zxrKxWrlQVw--DhaHEYQiuCgL0nyp6Ngfzf-pLMZfmWCjGhT6k6gCJDkD2qR6rBMV7kRablGadpi22IVY9fEsjvKLI_5_sesptI1NTm74TeY96WazBPuepNLtV9QQx3Y_TgO1X4jMKJ_b9Ia3X3Nt3a7FDfawXojNtXqRyINlL4_fzDFF7WQXpJfdQra4AzUHDnbsfc88PUVkY5Y4GJYVUGv_3nOo3InMnl-y1r-7k_g"

echo "🔐 Testing Google Token Authentication for YakRooms"
echo "=================================================="
echo ""

# Check if the application is running
echo "1. Checking if application is running..."
if curl -s "$BASE_URL/health/ping" > /dev/null; then
    echo "✅ Application is running at $BASE_URL"
else
    echo "❌ Application is not running. Please start the application first."
    echo "   Run: ./run-app-local.sh"
    exit 1
fi
echo ""

# Test 1: Firebase Authentication with Google Token
echo "2. Testing Firebase Authentication with Google Token..."
echo "   Endpoint: POST $BASE_URL/auth/firebase"
echo "   Token: ${GOOGLE_TOKEN:0:50}..."
echo ""

# Create temporary file for cookies
COOKIE_FILE=$(mktemp)

# Test Firebase authentication
FIREBASE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/firebase" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d "{\"idToken\": \"$GOOGLE_TOKEN\"}" \
    -c "$COOKIE_FILE")

# Extract response body and status code
FIREBASE_BODY=$(echo "$FIREBASE_RESPONSE" | head -n -1)
FIREBASE_STATUS=$(echo "$FIREBASE_RESPONSE" | tail -n 1)

echo "   Status Code: $FIREBASE_STATUS"
echo "   Response:"
echo "$FIREBASE_BODY" | jq . 2>/dev/null || echo "$FIREBASE_BODY"
echo ""

if [ "$FIREBASE_STATUS" = "200" ]; then
    echo "✅ Firebase authentication successful!"
    
    # Extract access token from response
    ACCESS_TOKEN=$(echo "$FIREBASE_BODY" | jq -r '.accessToken // empty' 2>/dev/null)
    if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "null" ]; then
        echo "✅ Access token received: ${ACCESS_TOKEN:0:50}..."
    else
        echo "⚠️  No access token in response (might be using cookies)"
    fi
    
    # Check for cookies
    echo "   Cookies received:"
    cat "$COOKIE_FILE" | grep -v "^#" | grep -v "^$" || echo "   No cookies found"
    echo ""
    
    # Test 2: Access Protected Endpoint
    echo "3. Testing access to protected endpoint..."
    echo "   Endpoint: GET $BASE_URL/api/hotels/list"
    echo ""
    
    # Try with Authorization header first
    if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "null" ]; then
        echo "   Using Authorization header..."
        PROTECTED_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/hotels/list" \
            -H "Authorization: Bearer $ACCESS_TOKEN" \
            -H "Accept: application/json")
    else
        echo "   Using cookies..."
        PROTECTED_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/hotels/list" \
            -H "Accept: application/json" \
            -b "$COOKIE_FILE")
    fi
    
    PROTECTED_BODY=$(echo "$PROTECTED_RESPONSE" | head -n -1)
    PROTECTED_STATUS=$(echo "$PROTECTED_RESPONSE" | tail -n 1)
    
    echo "   Status Code: $PROTECTED_STATUS"
    if [ "$PROTECTED_STATUS" = "200" ]; then
        echo "✅ Successfully accessed protected endpoint!"
        echo "   Response preview:"
        echo "$PROTECTED_BODY" | jq '. | length' 2>/dev/null || echo "   (Response is not JSON or empty)"
    else
        echo "❌ Failed to access protected endpoint"
        echo "   Response: $PROTECTED_BODY"
    fi
    echo ""
    
    # Test 3: Test Refresh Token
    echo "4. Testing refresh token functionality..."
    echo "   Endpoint: POST $BASE_URL/auth/refresh-token"
    echo ""
    
    REFRESH_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/refresh-token" \
        -H "Content-Type: application/json" \
        -H "Accept: application/json" \
        -b "$COOKIE_FILE")
    
    REFRESH_BODY=$(echo "$REFRESH_RESPONSE" | head -n -1)
    REFRESH_STATUS=$(echo "$REFRESH_RESPONSE" | tail -n 1)
    
    echo "   Status Code: $REFRESH_STATUS"
    echo "   Response:"
    echo "$REFRESH_BODY" | jq . 2>/dev/null || echo "$REFRESH_BODY"
    echo ""
    
    if [ "$REFRESH_STATUS" = "200" ]; then
        echo "✅ Refresh token functionality working!"
    else
        echo "⚠️  Refresh token test failed (this might be expected if no refresh token was set)"
    fi
    
    # Test 4: Test Logout
    echo "5. Testing logout functionality..."
    echo "   Endpoint: POST $BASE_URL/auth/logout"
    echo ""
    
    LOGOUT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/logout" \
        -H "Content-Type: application/json" \
        -H "Accept: application/json" \
        -b "$COOKIE_FILE")
    
    LOGOUT_BODY=$(echo "$LOGOUT_RESPONSE" | head -n -1)
    LOGOUT_STATUS=$(echo "$LOGOUT_RESPONSE" | tail -n 1)
    
    echo "   Status Code: $LOGOUT_STATUS"
    echo "   Response:"
    echo "$LOGOUT_BODY" | jq . 2>/dev/null || echo "$LOGOUT_BODY"
    echo ""
    
    if [ "$LOGOUT_STATUS" = "200" ]; then
        echo "✅ Logout functionality working!"
    else
        echo "⚠️  Logout test failed"
    fi
    
else
    echo "❌ Firebase authentication failed!"
    echo "   This could be due to:"
    echo "   - Invalid or expired Google token"
    echo "   - Firebase configuration issues"
    echo "   - Network connectivity problems"
    echo "   - Application not properly configured"
fi

# Cleanup
rm -f "$COOKIE_FILE"

echo ""
echo "🏁 Authentication test completed!"
echo "=================================================="
