#!/bin/bash

echo "🔐 Comprehensive Endpoint Security Test Report - FIXED VERSION"
echo "============================================================="
echo "Testing all endpoints for Spring Security & JWT authentication"
echo ""

BASE_URL="http://localhost:8080"
REPORT_FILE="endpoint_security_report_fixed.txt"

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

# Clear previous report
> "$REPORT_FILE"
echo "Endpoint | Auth Required | Status | Response Time | Result | Description" >> "$REPORT_FILE"
echo "---------|---------------|---------|---------------|---------|-------------" >> "$REPORT_FILE"

echo "🚀 Starting Comprehensive Endpoint Testing - FIXED VERSION..."
echo ""

# Test key public endpoints
echo "1️⃣ TESTING PUBLIC ENDPOINTS"
echo "============================"
test_endpoint "GET" "/api/test-data/status" "Public" "Test data controller status"
test_endpoint "GET" "/api/hotels/topThree" "Public" "Get top 3 hotels for homepage display"
test_endpoint "GET" "/api/hotels" "Public" "Get all hotels with pagination"
test_endpoint "GET" "/api/rooms/available/1" "Public" "Get available rooms for booking"
test_endpoint "GET" "/api/reviews/averageRating" "Public" "Get average rating across platform"
test_endpoint "POST" "/api/getIntouch" "Public" "Contact form submission" '{"name":"Test","email":"test@test.com"}'
echo ""

# Test key protected endpoints
echo "2️⃣ TESTING PROTECTED ENDPOINTS"
echo "==============================="
test_endpoint "GET" "/api/hotels/superAdmin" "Protected" "Get all hotels for super admin management"
test_endpoint "GET" "/api/staff/hotel/1" "Protected" "Get staff by hotel ID"
test_endpoint "POST" "/api/reviews" "Protected" "Submit hotel review" '{"hotelId":1,"rating":5}'
echo ""

# Test JWT endpoints
echo "3️⃣ TESTING JWT ENDPOINTS"
echo "========================="
test_endpoint "GET" "/api/jwt-test/tokens" "Public" "Get all test JWT tokens"
test_endpoint "GET" "/api/jwt-test/token/SUPER_ADMIN" "Public" "Get SUPER_ADMIN test token"
echo ""

# Generate final report
echo "📊 COMPREHENSIVE TEST REPORT - FIXED VERSION"
echo "============================================"
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
echo "============================================"
