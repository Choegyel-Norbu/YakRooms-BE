#!/bin/bash

# Test script to verify cookie configuration fix
# This will test if cookies are properly set with SameSite attributes

echo "=== Testing Cookie Configuration Fix ==="
echo "Testing if cookies are properly set with SameSite attributes..."
echo

# Test 1: Check if server is running
echo "1. Checking if server is running..."
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health
if [ $? -eq 0 ]; then
    echo "✓ Server is running"
else
    echo "✗ Server is not running. Please start the application first."
    exit 1
fi

echo
echo "2. Testing authentication endpoint (will fail but should show cookie headers)..."
curl -X POST http://localhost:8080/auth/firebase \
  -H "Content-Type: application/json" \
  -H "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36" \
  -H "Origin: http://localhost:3000" \
  -d '{"idToken":"mock-firebase-token"}' \
  -v \
  -c cookies.txt \
  -b cookies.txt 2>&1 | grep -E "(Set-Cookie|Cookie|HTTP/)"

echo
echo "3. Checking cookies file for SameSite attributes..."
if [ -f cookies.txt ]; then
    echo "Cookies file contents:"
    cat cookies.txt
    echo
    if grep -q "SameSite" cookies.txt; then
        echo "✓ SameSite attribute found in cookies"
    else
        echo "✗ SameSite attribute NOT found in cookies"
    fi
else
    echo "✗ No cookies file created"
fi

echo
echo "4. Testing with a valid Firebase token (if available)..."
echo "Note: This requires a real Firebase token to test the full flow"
echo "You can test this manually with your frontend application"

echo
echo "=== Cookie Fix Test Complete ==="

# Clean up
rm -f cookies.txt
