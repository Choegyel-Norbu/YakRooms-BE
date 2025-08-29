#!/bin/bash

echo "🔐 Comprehensive Endpoint Security Test Report"
echo "=============================================="
echo "Testing all endpoints for Spring Security & JWT authentication"
echo ""

BASE_URL="http://localhost:8080"
REPORT_FILE="endpoint_security_report.txt"

# Initialize counters
TOTAL_ENDPOINTS=0
PUBLIC_ENDPOINTS=0
PROTECTED_ENDPOINTS=0
SUCCESS_TESTS=0
FAILED_TESTS=0

# Function to test endpoint and log results
test_endpoint() {
    local method=$1
    local endpoint=$2
    local expected_auth=$3
    local description=$4
    local data=$5
    
    TOTAL_ENDPOINTS=$((TOTAL_ENDPOINTS + 1))
    
    echo "Testing: $method $endpoint"
    echo "Expected: $expected_auth"
    echo "Description: $description"
    
    # Prepare curl command
    local curl_cmd="curl -s -w '|HTTP_STATUS:%{http_code}|RESPONSE_TIME:%{time_total}s'"
    
    if [ "$method" = "POST" ]; then
        curl_cmd="$curl_cmd -X POST -H 'Content-Type: application/json'"
        if [ -n "$data" ]; then
            curl_cmd="$curl_cmd -d '$data'"
        fi
    elif [ "$method" = "PUT" ]; then
        curl_cmd="$curl_cmd -X PUT -H 'Content-Type: application/json'"
        if [ -n "$data" ]; then
            curl_cmd="$curl_cmd -d '$data'"
        fi
    elif [ "$method" = "DELETE" ]; then
        curl_cmd="$curl_cmd -X DELETE"
    fi
    
    curl_cmd="$curl_cmd '$BASE_URL$endpoint'"
    
    # Execute test
    local result=$(eval $curl_cmd 2>/dev/null)
    local http_status=$(echo "$result" | grep -o 'HTTP_STATUS:[0-9]*' | cut -d: -f2)
    local response_time=$(echo "$result" | grep -o 'RESPONSE_TIME:[0-9.]*' | cut -d: -f2)
    local response_body=$(echo "$result" | sed 's/|HTTP_STATUS:[0-9]*|RESPONSE_TIME:[0-9.]*s//')
    
    # Determine test result
    local test_result=""
    if [ "$expected_auth" = "Public" ]; then
        if [ "$http_status" = "200" ]; then
            test_result="✅ PASS"
            SUCCESS_TESTS=$((SUCCESS_TESTS + 1))
            PUBLIC_ENDPOINTS=$((PUBLIC_ENDPOINTS + 1))
        else
            test_result="❌ FAIL (Expected 200, got $http_status)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        if [ "$http_status" = "401" ]; then
            test_result="✅ PASS (Correctly protected)"
            SUCCESS_TESTS=$((SUCCESS_TESTS + 1))
            PROTECTED_ENDPOINTS=$((PROTECTED_ENDPOINTS + 1))
        elif [ "$http_status" = "403" ]; then
            test_result="✅ PASS (Correctly protected with role check)"
            SUCCESS_TESTS=$((SUCCESS_TESTS + 1))
            PROTECTED_ENDPOINTS=$((PROTECTED_ENDPOINTS + 1))
        else
            test_result="❌ FAIL (Expected 401/403, got $http_status)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    fi
    
    echo "Result: $test_result"
    echo "Status: $http_status"
    echo "Response Time: ${response_time}s"
    echo "Response: ${response_body:0:100}..."
    echo "----------------------------------------"
    
    # Log to report file
    echo "$method $endpoint | $expected_auth | $http_status | ${response_time}s | $test_result | $description" >> "$REPORT_FILE"
}

# Function to test with JWT token
test_with_jwt() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4
    
    echo "Testing with JWT: $method $endpoint"
    echo "Description: $description"
    
    # Test with invalid JWT token
    local curl_cmd="curl -s -w '|HTTP_STATUS:%{http_code}|RESPONSE_TIME:%{time_total}s'"
    
    if [ "$method" = "POST" ]; then
        curl_cmd="$curl_cmd -X POST -H 'Content-Type: application/json'"
        if [ -n "$data" ]; then
            curl_cmd="$curl_cmd -d '$data'"
        fi
    elif [ "$method" = "PUT" ]; then
        curl_cmd="$curl_cmd -X PUT -H 'Content-Type: application/json'"
        if [ -n "$data" ]; then
            curl_cmd="$curl_cmd -d '$data'"
        fi
    elif [ "$method" = "DELETE" ]; then
        curl_cmd="$curl_cmd -X DELETE"
    fi
    
    curl_cmd="$curl_cmd -H 'Authorization: Bearer invalid.jwt.token' '$BASE_URL$endpoint'"
    
    local result=$(eval $curl_cmd 2>/dev/null)
    local http_status=$(echo "$result" | grep -o 'HTTP_STATUS:[0-9]*' | cut -d: -f2)
    local response_time=$(echo "$result" | grep -o 'RESPONSE_TIME:[0-9.]*' | cut -d: -f2)
    
    if [ "$http_status" = "401" ]; then
        echo "Result: ✅ PASS (JWT validation working)"
        SUCCESS_TESTS=$((SUCCESS_TESTS + 1))
    else
        echo "Result: ❌ FAIL (JWT validation not working, got $http_status)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    echo "Status: $http_status"
    echo "Response Time: ${response_time}s"
    echo "----------------------------------------"
}

# Clear previous report
> "$REPORT_FILE"
echo "Endpoint | Auth Required | Status | Response Time | Result | Description" >> "$REPORT_FILE"
echo "---------|---------------|---------|---------------|---------|-------------" >> "$REPORT_FILE"

echo "🚀 Starting Comprehensive Endpoint Testing..."
echo ""

# 1. AUTHENTICATION ENDPOINTS
echo "1️⃣ AUTHENTICATION ENDPOINTS"
echo "=========================="
test_endpoint "POST" "/auth/firebase" "Public" "Firebase authentication for all users" '{"idToken":"test"}'
echo ""

# 2. HOTEL ENDPOINTS
echo "2️⃣ HOTEL MANAGEMENT ENDPOINTS"
echo "============================="
test_endpoint "GET" "/api/hotels/topThree" "Public" "Get top 3 hotels for homepage display"
test_endpoint "GET" "/api/hotels/details/1" "Public" "Get detailed hotel information by ID"
test_endpoint "GET" "/api/hotels" "Public" "Get all hotels with pagination"
test_endpoint "GET" "/api/hotels/search" "Public" "Search hotels by criteria"
test_endpoint "GET" "/api/hotels/sortedByLowestPrice" "Public" "Get hotels sorted by lowest price"
test_endpoint "GET" "/api/hotels/sortedByHighestPrice" "Public" "Get hotels sorted by highest price"
test_endpoint "GET" "/api/hotels/superAdmin" "Protected" "Get all hotels for super admin management"
test_endpoint "POST" "/api/hotels/1/verify" "Protected" "Verify a hotel (admin approval)" '{"verified":true}'
test_endpoint "GET" "/api/hotels/1" "Protected" "Get hotel information by user ID"
test_endpoint "POST" "/api/hotels/1" "Protected" "Create new hotel listing" '{"name":"Test Hotel"}'
test_endpoint "PUT" "/api/hotels/1" "Protected" "Update hotel information" '{"name":"Updated Hotel"}'
test_endpoint "DELETE" "/api/hotels/1" "Protected" "Delete hotel and all associated data"
echo ""

# 3. ROOM ENDPOINTS
echo "3️⃣ ROOM MANAGEMENT ENDPOINTS"
echo "============================="
test_endpoint "GET" "/api/rooms/1" "Public" "Get room by ID"
test_endpoint "GET" "/api/rooms/available/1" "Public" "Get available rooms for booking"
test_endpoint "GET" "/api/rooms/hotel/1" "Protected" "Get all rooms for a specific hotel"
test_endpoint "POST" "/api/rooms/hotel/1" "Protected" "Create new room for hotel" '{"roomNumber":"101"}'
test_endpoint "PUT" "/api/rooms/1" "Protected" "Update room information" '{"roomNumber":"101A"}'
test_endpoint "DELETE" "/api/rooms/1" "Protected" "Delete room"
test_endpoint "PATCH" "/api/rooms/1/availability" "Protected" "Toggle room availability" "?isAvailable=false"
test_endpoint "GET" "/api/rooms/status/1" "Protected" "Get room status for hotel management"
test_endpoint "GET" "/api/rooms/status/1/search" "Protected" "Search rooms by room number" "?roomNumber=101"
echo ""

# 4. BOOKING ENDPOINTS
echo "4️⃣ BOOKING MANAGEMENT ENDPOINTS"
echo "==============================="
test_endpoint "GET" "/api/bookings/availability" "Public" "Check room availability"
test_endpoint "POST" "/api/bookings" "Protected" "Create new booking" '{"roomId":1,"hotelId":1}'
test_endpoint "POST" "/api/bookings/1/cancel" "Protected" "Cancel booking" "?userId=1"
test_endpoint "POST" "/api/bookings/1/confirm" "Protected" "Confirm booking"
test_endpoint "PUT" "/api/bookings/1/status/confirmed" "Protected" "Update booking status"
test_endpoint "PUT" "/api/bookings/1/status/checked_in" "Protected" "Check-in guest"
test_endpoint "DELETE" "/api/bookings/1" "Protected" "Cancel/delete booking"
test_endpoint "GET" "/api/bookings/" "Protected" "Get paginated bookings for hotel"
test_endpoint "GET" "/api/bookings/user/1" "Protected" "Get user's booking history"
test_endpoint "GET" "/api/bookings/user/1/page" "Protected" "Get user's booking history with pagination"
test_endpoint "GET" "/api/bookings/user/1/status/confirmed" "Protected" "Get user's bookings by status"
test_endpoint "GET" "/api/bookings/hotel/1" "Protected" "Get all bookings for a hotel"
test_endpoint "GET" "/api/bookings/1" "Protected" "Get specific booking details"
test_endpoint "GET" "/api/bookings/debug/room/1/capacity" "Protected" "Debug endpoint to check room capacity"
echo ""

# 5. STAFF ENDPOINTS
echo "5️⃣ STAFF MANAGEMENT ENDPOINTS"
echo "============================="
test_endpoint "POST" "/api/staff" "Protected" "Add new staff member" '{"email":"staff@test.com"}'
test_endpoint "GET" "/api/staff/hotel/1/page" "Protected" "Get staff by hotel ID with pagination"
test_endpoint "GET" "/api/staff/hotel/1" "Protected" "Get staff by hotel ID"
test_endpoint "DELETE" "/api/staff/1" "Protected" "Remove staff member"
echo ""

# 6. REVIEW ENDPOINTS
echo "6️⃣ REVIEW & RATING ENDPOINTS"
echo "============================"
test_endpoint "GET" "/api/reviews/hotel/1/testimonials/paginated" "Public" "Get hotel testimonials with pagination"
test_endpoint "GET" "/api/reviews/hotel/1/average-rating" "Public" "Get average rating for a hotel"
test_endpoint "GET" "/api/reviews/hotel/1/review-count" "Public" "Get number of reviews for a hotel"
test_endpoint "GET" "/api/reviews/averageRating" "Public" "Get average rating across platform"
test_endpoint "POST" "/api/reviews" "Protected" "Submit hotel review" '{"hotelId":1,"rating":5}'
test_endpoint "POST" "/api/reviews/rating" "Protected" "Submit rating" '{"stars":5,"feedback":"Great!"}'
echo ""

# 7. NOTIFICATION ENDPOINTS
echo "7️⃣ NOTIFICATION ENDPOINTS"
echo "========================="
test_endpoint "GET" "/api/notifications/user/1" "Protected" "Get user notifications"
test_endpoint "PUT" "/api/notifications/user/1/markAllRead" "Protected" "Mark all user notifications as read"
test_endpoint "DELETE" "/api/notifications/user/1" "Protected" "Delete all user notifications"
echo ""

# 8. ANALYTICS ENDPOINTS
echo "8️⃣ ANALYTICS ENDPOINTS"
echo "======================"
test_endpoint "GET" "/api/booking-statistics/monthly" "Protected" "Get monthly booking trends" "?startDate=2024-01-01"
test_endpoint "GET" "/api/booking-statistics/monthly/hotel/1" "Protected" "Get monthly booking trends for hotel" "?startDate=2024-01-01"
test_endpoint "GET" "/api/booking-statistics/revenue/monthly/1" "Protected" "Get monthly revenue statistics" "?startDate=2024-01-01"
echo ""

# 9. SECURITY ENDPOINTS
echo "9️⃣ SECURITY & VERIFICATION ENDPOINTS"
echo "===================================="
test_endpoint "GET" "/api/passcode/verify" "Protected" "Verify check-in passcode" "?passcode=123456"
test_endpoint "POST" "/api/passcode/verify" "Protected" "Verify check-in passcode" "?passcode=123456"
echo ""

# 10. USER ROLE ENDPOINTS
echo "🔟 USER ROLE MANAGEMENT ENDPOINTS"
echo "================================="
test_endpoint "POST" "/api/user-roles/1/roles/ADMIN" "Protected" "Add role to user"
test_endpoint "DELETE" "/api/user-roles/1/roles/ADMIN" "Protected" "Remove role from user"
test_endpoint "GET" "/api/user-roles/1/roles" "Protected" "Get user roles"
test_endpoint "GET" "/api/user-roles/1/roles/ADMIN" "Protected" "Check if user has specific role"
test_endpoint "PUT" "/api/user-roles/1/roles" "Protected" "Set user roles" '["ADMIN","STAFF"]'
echo ""

# 11. CONTACT ENDPOINTS
echo "1️⃣1️⃣ CONTACT ENDPOINTS"
echo "======================"
test_endpoint "POST" "/api/getIntouch" "Public" "Contact form submission" '{"name":"Test","email":"test@test.com"}'
echo ""

# 12. WEBSOCKET ENDPOINTS
echo "1️⃣2️⃣ WEBSOCKET ENDPOINTS"
echo "========================"
test_endpoint "GET" "/api/websocket/test" "Public" "WebSocket test endpoint"
echo ""

# Test JWT validation for protected endpoints
echo "🔐 TESTING JWT VALIDATION"
echo "========================="
test_with_jwt "GET" "/api/hotels/superAdmin" "Test JWT validation for super admin endpoint"
test_with_jwt "GET" "/api/staff/hotel/1" "Test JWT validation for staff endpoint"
test_with_jwt "POST" "/api/reviews" "Test JWT validation for review creation"
echo ""

# Generate final report
echo "📊 COMPREHENSIVE TEST REPORT"
echo "============================"
echo "Total Endpoints Tested: $TOTAL_ENDPOINTS"
echo "Public Endpoints: $PUBLIC_ENDPOINTS"
echo "Protected Endpoints: $PROTECTED_ENDPOINTS"
echo "Successful Tests: $SUCCESS_TESTS"
echo "Failed Tests: $FAILED_TESTS"
echo "Success Rate: $(( (SUCCESS_TESTS * 100) / TOTAL_ENDPOINTS ))%"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo "🎉 ALL TESTS PASSED! Spring Security and JWT are working perfectly!"
else
    echo "⚠️  $FAILED_TESTS tests failed. Check the detailed report above."
fi

echo ""
echo "📋 Detailed results saved to: $REPORT_FILE"
echo "=============================================="
