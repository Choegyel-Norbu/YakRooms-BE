#!/bin/bash

# Redis Testing Script for YakRooms Development
# This script tests Redis connectivity and caching functionality

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Redis container is running
check_redis_container() {
    if ! docker-compose ps redis | grep -q "Up"; then
        print_error "Redis container is not running. Start it with: docker-compose up -d redis"
        exit 1
    fi
    print_success "Redis container is running"
}

# Test Redis basic connectivity
test_redis_connectivity() {
    print_status "Testing Redis connectivity..."
    
    if docker-compose exec -T redis redis-cli ping | grep -q "PONG"; then
        print_success "Redis connectivity test passed"
    else
        print_error "Redis connectivity test failed"
        exit 1
    fi
}

# Test Redis operations
test_redis_operations() {
    print_status "Testing Redis operations..."
    
    # Test SET operation
    docker-compose exec -T redis redis-cli SET "yakrooms:test:connectivity" "OK" > /dev/null
    if [ $? -eq 0 ]; then
        print_success "Redis SET operation successful"
    else
        print_error "Redis SET operation failed"
        exit 1
    fi
    
    # Test GET operation
    result=$(docker-compose exec -T redis redis-cli GET "yakrooms:test:connectivity" 2>/dev/null | tr -d '\r\n')
    if [ "$result" = "OK" ]; then
        print_success "Redis GET operation successful"
    else
        print_error "Redis GET operation failed. Expected: 'OK', Got: '$result'"
        exit 1
    fi
    
    # Test TTL operation
    docker-compose exec -T redis redis-cli SETEX "yakrooms:test:ttl" 10 "TTL Test" > /dev/null
    ttl=$(docker-compose exec -T redis redis-cli TTL "yakrooms:test:ttl" 2>/dev/null | tr -d '\r\n')
    if [ "$ttl" -gt 0 ] && [ "$ttl" -le 10 ]; then
        print_success "Redis TTL operation successful (TTL: ${ttl}s)"
    else
        print_error "Redis TTL operation failed. TTL: $ttl"
        exit 1
    fi
    
    # Clean up test keys
    docker-compose exec -T redis redis-cli DEL "yakrooms:test:connectivity" "yakrooms:test:ttl" > /dev/null
    print_success "Test keys cleaned up"
}

# Show Redis information
show_redis_info() {
    print_status "Redis Server Information:"
    echo ""
    
    # Redis version
    version=$(docker-compose exec -T redis redis-cli INFO server | grep redis_version | cut -d: -f2 | tr -d '\r\n')
    echo "  Version: $version"
    
    # Memory usage
    memory=$(docker-compose exec -T redis redis-cli INFO memory | grep used_memory_human | cut -d: -f2 | tr -d '\r\n')
    echo "  Memory Usage: $memory"
    
    # Connected clients
    clients=$(docker-compose exec -T redis redis-cli INFO clients | grep connected_clients | cut -d: -f2 | tr -d '\r\n')
    echo "  Connected Clients: $clients"
    
    # Database size
    db_size=$(docker-compose exec -T redis redis-cli DBSIZE | tr -d '\r\n')
    echo "  Database 0 Keys: $db_size"
    
    # Redis configuration
    echo ""
    print_status "Redis Configuration:"
    echo "  Host: localhost"
    echo "  Port: 6379"
    echo "  Database: 0"
    echo "  Password: (none)"
    echo "  Key Prefix: yakrooms:dev:"
}

# Test application cache keys
test_application_cache() {
    print_status "Testing application cache keys..."
    
    # Simulate cache keys that your application might use
    docker-compose exec -T redis redis-cli SET "yakrooms:dev:hotel-details:1" '{"id":1,"name":"Test Hotel","price":100}' > /dev/null
    docker-compose exec -T redis redis-cli SET "yakrooms:dev:hotel-listings:search" '{"results":[],"total":0}' > /dev/null
    docker-compose exec -T redis redis-cli SETEX "yakrooms:dev:search-results:test" 60 '{"query":"test","results":[]}' > /dev/null
    
    # Check if keys exist
    keys_count=$(docker-compose exec -T redis redis-cli KEYS "yakrooms:dev:*" | wc -l | tr -d ' ')
    if [ "$keys_count" -gt 0 ]; then
        print_success "Application cache keys created ($keys_count keys)"
        
        # Show cache keys
        echo ""
        print_status "Current cache keys:"
        docker-compose exec -T redis redis-cli KEYS "yakrooms:dev:*" | while read key; do
            ttl=$(docker-compose exec -T redis redis-cli TTL "$key" 2>/dev/null | tr -d '\r\n')
            if [ "$ttl" = "-1" ]; then
                echo "  $key (no expiration)"
            else
                echo "  $key (TTL: ${ttl}s)"
            fi
        done
    else
        print_error "No application cache keys found"
    fi
}

# Interactive Redis CLI
interactive_redis_cli() {
    print_status "Starting interactive Redis CLI..."
    print_warning "Type 'exit' to return to the script"
    echo ""
    docker-compose exec redis redis-cli
}

# Main function
main() {
    echo "🔍 YakRooms Redis Testing Script"
    echo "================================="
    echo ""
    
    check_redis_container
    test_redis_connectivity
    test_redis_operations
    show_redis_info
    test_application_cache
    
    echo ""
    print_success "All Redis tests completed successfully!"
    echo ""
    
    # Ask if user wants to open interactive CLI
    read -p "Do you want to open interactive Redis CLI? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        interactive_redis_cli
    fi
}

# Handle command line arguments
case "${1:-}" in
    "info")
        check_redis_container
        show_redis_info
        ;;
    "test")
        check_redis_container
        test_redis_connectivity
        test_redis_operations
        test_application_cache
        print_success "All tests passed!"
        ;;
    "cli")
        check_redis_container
        interactive_redis_cli
        ;;
    "keys")
        check_redis_container
        print_status "Current Redis keys:"
        docker-compose exec -T redis redis-cli KEYS "*"
        ;;
    "monitor")
        check_redis_container
        print_status "Starting Redis monitor (Ctrl+C to stop)..."
        docker-compose exec redis redis-cli MONITOR
        ;;
    "help"|"-h"|"--help")
        echo "YakRooms Redis Testing Script"
        echo ""
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  (no args)  - Run all tests and show info"
        echo "  info       - Show Redis server information"
        echo "  test       - Run connectivity and operation tests"
        echo "  cli        - Open interactive Redis CLI"
        echo "  keys       - Show all Redis keys"
        echo "  monitor    - Start Redis monitor (real-time commands)"
        echo "  help       - Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0              # Run all tests"
        echo "  $0 info         # Show Redis info"
        echo "  $0 cli          # Open Redis CLI"
        echo "  $0 monitor      # Monitor Redis commands"
        exit 0
        ;;
    "")
        main
        ;;
    *)
        print_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
