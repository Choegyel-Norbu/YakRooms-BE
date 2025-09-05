#!/bin/bash

# Docker Health Check Test Script
# This script tests the Docker deployment and health endpoints

set -e

echo "=========================================="
echo "YakRooms Docker Health Check Test"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "SUCCESS" ]; then
        echo -e "${GREEN}✓${NC} $message"
    elif [ "$status" = "ERROR" ]; then
        echo -e "${RED}✗${NC} $message"
    elif [ "$status" = "INFO" ]; then
        echo -e "${YELLOW}ℹ${NC} $message"
    fi
}

# Function to test endpoint
test_endpoint() {
    local url=$1
    local expected_status=$2
    local description=$3
    
    echo "Testing: $description"
    echo "URL: $url"
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    
    if [ "$response" = "$expected_status" ]; then
        print_status "SUCCESS" "$description - Status: $response"
        return 0
    else
        print_status "ERROR" "$description - Expected: $expected_status, Got: $response"
        return 1
    fi
}

# Function to test endpoint with JSON response
test_endpoint_json() {
    local url=$1
    local expected_status=$2
    local description=$3
    
    echo "Testing: $description"
    echo "URL: $url"
    
    response=$(curl -s -w "%{http_code}" "$url" || echo "000")
    status_code="${response: -3}"
    json_response="${response%???}"
    
    if [ "$status_code" = "$expected_status" ]; then
        print_status "SUCCESS" "$description - Status: $status_code"
        echo "Response: $json_response"
        return 0
    else
        print_status "ERROR" "$description - Expected: $expected_status, Got: $status_code"
        echo "Response: $json_response"
        return 1
    fi
}

# Wait for application to be ready
wait_for_app() {
    local max_attempts=30
    local attempt=1
    
    print_status "INFO" "Waiting for application to start..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:8080/health/ping" > /dev/null 2>&1; then
            print_status "SUCCESS" "Application is ready after $attempt attempts"
            return 0
        fi
        
        echo "Attempt $attempt/$max_attempts - Application not ready yet..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    print_status "ERROR" "Application failed to start within $((max_attempts * 5)) seconds"
    return 1
}

# Main test execution
main() {
    echo "Starting Docker health check tests..."
    echo ""
    
    # Test basic connectivity
    print_status "INFO" "Testing basic connectivity endpoints..."
    echo ""
    
    test_endpoint "http://localhost:8080/health/ping" "200" "Health Ping Endpoint"
    echo ""
    
    # Test comprehensive health check
    print_status "INFO" "Testing comprehensive health endpoints..."
    echo ""
    
    test_endpoint_json "http://localhost:8080/health" "200" "Main Health Endpoint"
    echo ""
    
    test_endpoint_json "http://localhost:8080/health/ready" "200" "Readiness Endpoint"
    echo ""
    
    test_endpoint_json "http://localhost:8080/health/db" "200" "Database Health Endpoint"
    echo ""
    
    # Test Spring Boot Actuator endpoints
    print_status "INFO" "Testing Spring Boot Actuator endpoints..."
    echo ""
    
    test_endpoint_json "http://localhost:8080/actuator/health" "200" "Actuator Health Endpoint"
    echo ""
    
    # Test application endpoints
    print_status "INFO" "Testing application endpoints..."
    echo ""
    
    test_endpoint "http://localhost:8080/api/hotels" "401" "Hotels Endpoint (should require auth)"
    echo ""
    
    # Summary
    echo "=========================================="
    print_status "INFO" "Health check tests completed!"
    echo "=========================================="
}

# Check if Docker Compose is running
if ! docker-compose ps | grep -q "yakrooms-app"; then
    print_status "ERROR" "YakRooms application is not running. Please start it with: docker-compose up -d"
    exit 1
fi

# Run the tests
if wait_for_app; then
    main
else
    print_status "ERROR" "Application failed to start. Check logs with: docker-compose logs yakrooms-app"
    exit 1
fi
