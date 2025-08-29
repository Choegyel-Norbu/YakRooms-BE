#!/bin/bash

echo "🔐 Testing Spring Security & JWT Authentication"
echo "================================================"

BASE_URL="http://localhost:8080"

echo ""
echo "1️⃣ Testing Public Endpoints (Should Work Without Token)"
echo "--------------------------------------------------------"

echo "Testing /api/hotels/topThree..."
curl -s -w " | Status: %{http_code}\n" "$BASE_URL/api/hotels/topThree"

echo ""
echo "Testing /api/hotels/details/1..."
curl -s -w " | Status: %{http_code}\n" "$BASE_URL/api/hotels/details/1"

echo ""
echo "2️⃣ Testing Protected Endpoints (Should Return 401 Without Token)"
echo "----------------------------------------------------------------"

echo "Testing /api/hotels/superAdmin (SUPER_ADMIN only)..."
curl -s -w " | Status: %{http_code}\n" "$BASE_URL/api/hotels/superAdmin"

echo ""
echo "Testing /api/staff/hotel/1 (HOTEL_ADMIN/STAFF only)..."
curl -s -w " | Status: %{http_code}\n" "$BASE_URL/api/staff/hotel/1"

echo ""
echo "Testing /api/bookings/ (HOTEL_ADMIN/STAFF only)..."
curl -s -w " | Status: %{http_code}\n" "$BASE_URL/api/bookings/"

echo ""
echo "Testing /api/reviews (GUEST only)..."
curl -s -w " | Status: %{http_code}\n" "$BASE_URL/api/reviews"

echo ""
echo "3️⃣ Testing JWT Token Generation"
echo "--------------------------------"

echo "Testing Firebase authentication endpoint..."
curl -s -w " | Status: %{http_code}\n" \
  -X POST "$BASE_URL/auth/firebase" \
  -H "Content-Type: application/json" \
  -d '{"idToken":"test-token"}'

echo ""
echo "4️⃣ Testing with Invalid JWT Token"
echo "-----------------------------------"

echo "Testing with invalid JWT token..."
curl -s -w " | Status: %{http_code}\n" \
  -H "Authorization: Bearer invalid.jwt.token" \
  "$BASE_URL/api/hotels/superAdmin"

echo ""
echo "✅ Test Complete!"
echo ""
echo "Expected Results:"
echo "- Public endpoints: Status 200"
echo "- Protected endpoints without token: Status 401"
echo "- Firebase auth: Status 200 (or 400 if token invalid)"
echo "- Protected endpoints with invalid token: Status 401"
echo ""
echo "If you see 401 responses for protected endpoints, Spring Security is working!"
echo "If you see 403 responses, role-based authorization is working!"
