# YakRooms Docker Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying YakRooms using Docker with proper health checks, monitoring, and production-grade configuration.

## Architecture

### Health Check Strategy

We implement a **three-tier health check strategy**:

1. **Liveness Probe** (`/health/ping`): Simple connectivity check
2. **Readiness Probe** (`/health/ready`): Database connectivity verification
3. **Comprehensive Health** (`/health`): Detailed system status

### Container Orchestration

- **Startup Period**: 120 seconds for application initialization
- **Health Check Interval**: 30 seconds
- **Timeout**: 15 seconds per check
- **Retries**: 5 attempts before marking unhealthy

## Quick Start

### 1. Local Development with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check health status
./test-docker-health.sh

# View logs
docker-compose logs -f yakrooms-app

# Stop services
docker-compose down
```

### 2. Production Deployment

```bash
# Build the application
docker build -t yakrooms:latest .

# Run with environment variables
docker run -d \
  --name yakrooms-app \
  -p 8080:8080 \
  -e MYSQLHOST=your-mysql-host \
  -e MYSQLUSER=your-mysql-user \
  -e MYSQLPASSWORD=your-mysql-password \
  -e REDIS_HOST=your-redis-host \
  yakrooms:latest
```

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MYSQLHOST` | MySQL host | `mysql` | Yes |
| `MYSQLPORT` | MySQL port | `3306` | No |
| `MYSQLDATABASE` | Database name | `yakrooms` | No |
| `MYSQLUSER` | Database user | `root` | No |
| `MYSQLPASSWORD` | Database password | - | Yes |
| `REDIS_HOST` | Redis host | `redis` | No |
| `REDIS_PORT` | Redis port | `6379` | No |
| `REDIS_PASSWORD` | Redis password | - | No |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `docker` | No |

### Health Check Endpoints

| Endpoint | Purpose | Response |
|----------|---------|----------|
| `/health/ping` | Basic connectivity | `200 OK` |
| `/health/ready` | Readiness check | `200 OK` or `503 Service Unavailable` |
| `/health` | Comprehensive status | `200 OK` with detailed JSON |
| `/health/db` | Database status | `200 OK` or `503 Service Unavailable` |
| `/actuator/health` | Spring Boot Actuator | `200 OK` with actuator JSON |

## Troubleshooting

### Common Issues

#### 1. Health Check Failures

**Symptoms**: Container keeps restarting, health checks fail

**Solutions**:
```bash
# Check application logs
docker logs yakrooms-app

# Test health endpoints manually
curl http://localhost:8080/health/ping
curl http://localhost:8080/health/ready

# Check database connectivity
docker exec yakrooms-app curl http://localhost:8080/health/db
```

#### 2. Database Connection Issues

**Symptoms**: Database health check fails, application logs show connection errors

**Solutions**:
```bash
# Verify database is running
docker-compose ps mysql

# Check database logs
docker-compose logs mysql

# Test database connection
docker exec yakrooms-mysql mysql -u root -pChogyalWp -e "SELECT 1"
```

#### 3. Startup Timeout

**Symptoms**: Application takes too long to start, exceeds startup period

**Solutions**:
- Increase `start_period` in Dockerfile healthcheck
- Check for slow database migrations
- Verify Redis connectivity
- Review application logs for initialization issues

### Debugging Commands

```bash
# Check container status
docker ps -a

# View detailed container info
docker inspect yakrooms-app

# Check health check history
docker inspect yakrooms-app | grep -A 10 Health

# Monitor logs in real-time
docker logs -f yakrooms-app

# Execute commands in running container
docker exec -it yakrooms-app sh
```

## Production Considerations

### Security

- **Non-root User**: Application runs as `appuser` (UID 1000)
- **Minimal Base Image**: Alpine Linux for smaller attack surface
- **Secrets Management**: Use Docker secrets or external secret management
- **Network Security**: Use Docker networks for service isolation

### Performance

- **Resource Limits**: Set appropriate CPU and memory limits
- **Connection Pooling**: Configured HikariCP for optimal database connections
- **Caching**: Redis integration for improved performance
- **Health Check Optimization**: Reduced timeouts for faster failure detection

### Monitoring

- **Health Metrics**: Comprehensive health endpoint with component status
- **Logging**: Structured logging with appropriate log levels
- **Metrics**: Spring Boot Actuator for application metrics
- **Alerting**: Configure alerts based on health check failures

## Railway Deployment

### Configuration

The `railway.toml` file is configured for Railway deployment:

```toml
[deploy]
startCommand = "java -Dspring.profiles.active=docker -jar target/*.jar"
healthcheckPath = "/health/ping"
healthcheckTimeout = 300
restartPolicyType = "on_failure"
```

### Environment Variables

Set these in Railway dashboard:
- `MYSQLHOST`: Your MySQL host
- `MYSQLUSER`: Database username
- `MYSQLPASSWORD`: Database password
- `REDIS_HOST`: Redis host (if using Redis)

## Testing

### Automated Health Check Test

Run the comprehensive test suite:

```bash
./test-docker-health.sh
```

This script tests:
- Basic connectivity
- Health endpoints
- Database connectivity
- Application endpoints
- Spring Boot Actuator

### Manual Testing

```bash
# Test basic health
curl http://localhost:8080/health/ping

# Test readiness
curl http://localhost:8080/health/ready

# Test comprehensive health
curl http://localhost:8080/health | jq

# Test database health
curl http://localhost:8080/health/db | jq
```

## Best Practices

### 1. Health Check Design

- **Fast Response**: Health checks should respond quickly (< 1 second)
- **Minimal Dependencies**: Don't check external services in liveness probes
- **Graceful Degradation**: Return appropriate status codes
- **Detailed Information**: Provide useful diagnostic information

### 2. Container Configuration

- **Resource Limits**: Set appropriate CPU and memory limits
- **Restart Policies**: Use `unless-stopped` for production
- **Health Check Timing**: Balance between responsiveness and stability
- **Logging**: Ensure logs are accessible and structured

### 3. Database Considerations

- **Connection Pooling**: Configure appropriate pool sizes
- **Timeout Settings**: Set reasonable connection timeouts
- **Migration Strategy**: Use Flyway for database migrations
- **Backup Strategy**: Implement regular database backups

### 4. Monitoring and Alerting

- **Health Metrics**: Monitor health check success rates
- **Performance Metrics**: Track response times and resource usage
- **Error Rates**: Monitor application error rates
- **Dependency Health**: Monitor database and Redis connectivity

## Support

For issues or questions:

1. Check the application logs: `docker logs yakrooms-app`
2. Run the health check test: `./test-docker-health.sh`
3. Review this guide for troubleshooting steps
4. Check the Railway deployment logs if using Railway

## Changelog

### v1.0.0
- Initial Docker configuration
- Three-tier health check strategy
- Docker Compose setup
- Comprehensive testing suite
- Production-ready configuration
