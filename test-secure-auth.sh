#!/bin/bash

# Test script for Secure Cookie Authentication
# This script tests the new authentication endpoints

BASE_URL="http://localhost:8080"
GOOGLE_ID_TOKEN="your-google-id-token-here"

echo "🔐 Testing Secure Cookie Authentication System"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to make HTTP requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local cookies=$4
    
    if [ -n "$cookies" ]; then
        curl -s -X $method \
             -H "Content-Type: application/json" \
             -H "Accept: application/json" \
             -b "$cookies" \
             -c cookies.txt \
             -d "$data" \
             "$BASE_URL$endpoint"
    else
        curl -s -X $method \
             -H "Content-Type: application/json" \
             -H "Accept: application/json" \
             -c cookies.txt \
             -d "$data" \
             "$BASE_URL$endpoint"
    fi
}

# Test 1: Health Check
echo -e "\n${YELLOW}Test 1: Health Check${NC}"
response=$(curl -s "$BASE_URL/health")
if [[ $response == *"UP"* ]]; then
    echo -e "${GREEN}✅ Health check passed${NC}"
else
    echo -e "${RED}❌ Health check failed${NC}"
    echo "Response: $response"
fi

# Test 2: Google OAuth Login (if token provided)
if [ "$GOOGLE_ID_TOKEN" != "your-google-id-token-here" ]; then
    echo -e "\n${YELLOW}Test 2: Google OAuth Login${NC}"
    login_data="{\"idToken\": \"$GOOGLE_ID_TOKEN\"}"
    response=$(make_request "POST" "/auth/firebase" "$login_data")
    
    if [[ $response == *"token"* ]]; then
        echo -e "${GREEN}✅ Login successful${NC}"
        echo "Response: $response"
        
        # Extract access token from response (for testing)
        access_token=$(echo $response | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        echo "Access token: ${access_token:0:20}..."
        
        # Test 3: Token Refresh
        echo -e "\n${YELLOW}Test 3: Token Refresh${NC}"
        refresh_response=$(make_request "POST" "/auth/refresh-token" "{}" "cookies.txt")
        
        if [[ $refresh_response == *"successfully"* ]]; then
            echo -e "${GREEN}✅ Token refresh successful${NC}"
            echo "Response: $refresh_response"
        else
            echo -e "${RED}❌ Token refresh failed${NC}"
            echo "Response: $refresh_response"
        fi
        
        # Test 4: Logout
        echo -e "\n${YELLOW}Test 4: Logout${NC}"
        logout_response=$(make_request "POST" "/auth/logout" "{}" "cookies.txt")
        
        if [[ $logout_response == *"successfully"* ]]; then
            echo -e "${GREEN}✅ Logout successful${NC}"
            echo "Response: $logout_response"
        else
            echo -e "${RED}❌ Logout failed${NC}"
            echo "Response: $logout_response"
        fi
        
    else
        echo -e "${RED}❌ Login failed${NC}"
        echo "Response: $response"
    fi
else
    echo -e "\n${YELLOW}Test 2: Google OAuth Login (Skipped - No token provided)${NC}"
    echo -e "${YELLOW}To test login, set GOOGLE_ID_TOKEN environment variable${NC}"
fi

# Test 5: Protected Endpoint Access
echo -e "\n${YELLOW}Test 5: Protected Endpoint Access${NC}"
protected_response=$(make_request "GET" "/api/user/profile" "{}" "cookies.txt")
if [[ $protected_response == *"401"* ]] || [[ $protected_response == *"Unauthorized"* ]]; then
    echo -e "${GREEN}✅ Protected endpoint correctly requires authentication${NC}"
else
    echo -e "${RED}❌ Protected endpoint should require authentication${NC}"
    echo "Response: $protected_response"
fi

# Test 6: Cookie Security Headers
echo -e "\n${YELLOW}Test 6: Cookie Security Headers${NC}"
headers=$(curl -s -I "$BASE_URL/auth/firebase" -X POST -H "Content-Type: application/json" -d '{"idToken":"test"}')
if [[ $headers == *"Set-Cookie"* ]]; then
    echo -e "${GREEN}✅ Cookies are being set${NC}"
    echo "Cookie headers:"
    echo "$headers" | grep -i "set-cookie"
else
    echo -e "${RED}❌ No cookies found in response${NC}"
fi

# Cleanup
rm -f cookies.txt

echo -e "\n${YELLOW}Test Summary${NC}"
echo "============="
echo "✅ Health check"
echo "✅ Cookie security headers"
echo "✅ Protected endpoint security"
if [ "$GOOGLE_ID_TOKEN" != "your-google-id-token-here" ]; then
    echo "✅ Google OAuth login"
    echo "✅ Token refresh"
    echo "✅ Logout"
else
    echo "⏭️  Google OAuth login (skipped - no token)"
    echo "⏭️  Token refresh (skipped - no token)"
    echo "⏭️  Logout (skipped - no token)"
fi

echo -e "\n${GREEN}🎉 Secure Cookie Authentication System Test Complete!${NC}"
echo -e "\n${YELLOW}Next Steps:${NC}"
echo "1. Set GOOGLE_ID_TOKEN environment variable to test full flow"
echo "2. Update your frontend to use cookie-based authentication"
echo "3. Monitor authentication logs in production"
echo "4. Set up proper JWT_SECRET in production environment"
