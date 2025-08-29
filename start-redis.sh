#!/bin/bash

# Redis Startup Script for YakRooms
echo "Starting Redis for YakRooms..."

# Check if Redis is already running
if pgrep -x "redis-server" > /dev/null; then
    echo "Redis is already running"
    exit 0
fi

# Start Redis with custom configuration
redis-server --appendonly yes --requirepass "${REDIS_PASSWORD:-}" --port 6379 --bind 0.0.0.0 &

# Wait for Redis to start
echo "Waiting for Redis to start..."
sleep 3

# Test Redis connection
if redis-cli -p 6379 ${REDIS_PASSWORD:+-a $REDIS_PASSWORD} ping | grep -q "PONG"; then
    echo "Redis started successfully on port 6379"
    echo "Redis status: $(redis-cli -p 6379 ${REDIS_PASSWORD:+-a $REDIS_PASSWORD} info server | grep 'redis_version')"
else
    echo "Failed to start Redis"
    exit 1
fi
