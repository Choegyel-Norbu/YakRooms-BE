#!/bin/bash

echo "Testing health endpoints..."

# Test Spring Boot Actuator health endpoint
echo "Testing /actuator/health..."
curl -s -w "Status: %{http_code}\n" http://localhost:8080/actuator/health

echo ""
echo "Testing /api/v1/uploadthing/health..."
curl -s -w "Status: %{http_code}\n" http://localhost:8080/api/v1/uploadthing/health

echo ""
echo "Health endpoint tests completed."
