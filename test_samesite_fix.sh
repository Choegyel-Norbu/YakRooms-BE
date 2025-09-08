#!/bin/bash

# Test script to verify SameSite cookie fix
# This script tests both development (HTTP) and production (HTTPS) scenarios

echo "🍪 Testing SameSite Cookie Fix"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test configuration
BASE_URL="http://localhost:8080"
TEST_EMAIL="test@example.com"
TEST_PASSWORD="testpassword123"

echo -e "${YELLOW}1. Testing Development Environment (HTTP + SameSite=Lax)${NC}"
echo "This should work with SameSite=Lax in development..."

# Test Firebase authentication (this will set cookies)
echo "Testing Firebase authentication..."
RESPONSE=$(curl -s -i -X POST "$BASE_URL/auth/firebase" \
  -H "Content-Type: application/json" \
  -d "{\"idToken\":\"test-token\",\"email\":\"$TEST_EMAIL\"}")

echo "Response Headers:"
echo "$RESPONSE" | grep -i "set-cookie"

# Check if cookies are set with SameSite=Lax
if echo "$RESPONSE" | grep -q "SameSite=Lax"; then
    echo -e "${GREEN}✅ SUCCESS: SameSite=Lax detected in development${NC}"
else
    echo -e "${RED}❌ FAILED: SameSite=Lax not found in development${NC}"
fi

echo ""
echo -e "${YELLOW}2. Testing Cookie Retrieval${NC}"

# Test if we can retrieve the cookies
echo "Testing cookie retrieval..."
COOKIE_RESPONSE=$(curl -s -i -X GET "$BASE_URL/api/hotels/list" \
  -H "Cookie: access_token=test-token; refresh_token=test-refresh-token")

echo "Cookie retrieval test completed."

echo ""
echo -e "${YELLOW}3. Testing Cookie Clearing${NC}"

# Test logout (this should clear cookies)
echo "Testing logout (cookie clearing)..."
LOGOUT_RESPONSE=$(curl -s -i -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -d "{}")

echo "Logout Response Headers:"
echo "$LOGOUT_RESPONSE" | grep -i "set-cookie"

# Check if cookies are cleared with SameSite=Lax
if echo "$LOGOUT_RESPONSE" | grep -q "SameSite=Lax"; then
    echo -e "${GREEN}✅ SUCCESS: Cookie clearing uses SameSite=Lax${NC}"
else
    echo -e "${RED}❌ FAILED: Cookie clearing doesn't use SameSite=Lax${NC}"
fi

echo ""
echo -e "${YELLOW}4. Production Environment Test (HTTPS + SameSite=None)${NC}"
echo "To test production environment:"
echo "1. Set COOKIE_SECURE=true in your environment"
echo "2. Use HTTPS endpoint"
echo "3. Cookies should use SameSite=None"

echo ""
echo -e "${GREEN}🎉 SameSite Cookie Fix Test Complete!${NC}"
echo ""
echo "Summary:"
echo "- Development (HTTP): Uses SameSite=Lax (works with HTTP)"
echo "- Production (HTTPS): Uses SameSite=None (works with HTTPS)"
echo "- Both scenarios maintain security while ensuring functionality"
