package com.yakrooms.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive health check controller that provides detailed health status.
 * Checks database connectivity when database is configured.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Comprehensive health check endpoint that verifies application and database status.
     * This endpoint provides detailed health information but should not fail
     * if external dependencies are temporarily unavailable during startup.
     * 
     * @return ResponseEntity with detailed health status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Basic application status - always UP if we reach this point
            healthStatus.put("status", "UP");
            healthStatus.put("application", "YakRooms Backend");
            healthStatus.put("timestamp", System.currentTimeMillis());
            healthStatus.put("version", "1.0.0");
            
            // Check database configuration and connectivity
            String dbHost = environment.getProperty("MYSQLHOST");
            if (dbHost != null && !dbHost.isEmpty() && dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(3)) { // Reduced timeout for faster response
                        healthStatus.put("database", "UP");
                        healthStatus.put("database_host", dbHost);
                        healthStatus.put("database_port", environment.getProperty("MYSQLPORT", "3306"));
                        healthStatus.put("database_name", environment.getProperty("MYSQLDATABASE", "yakrooms"));
                    } else {
                        healthStatus.put("database", "DOWN");
                        healthStatus.put("database_error", "Connection validation failed");
                    }
                } catch (Exception e) {
                    healthStatus.put("database", "DOWN");
                    healthStatus.put("database_error", e.getMessage());
                }
            } else {
                healthStatus.put("database", "NOT_CONFIGURED");
                healthStatus.put("database_note", "Database environment variables not set");
            }
            
            // Check Firebase configuration
            String firebaseConfig = environment.getProperty("FIREBASE_CONFIG_BASE64");
            if (firebaseConfig != null && !firebaseConfig.isEmpty()) {
                healthStatus.put("firebase", "CONFIGURED");
            } else {
                healthStatus.put("firebase", "NOT_CONFIGURED");
                healthStatus.put("firebase_note", "FIREBASE_CONFIG_BASE64 not set");
            }
            
            // Check Redis configuration - check Spring properties instead of env vars
            String redisHost = environment.getProperty("spring.data.redis.host");
            if (redisHost != null && !redisHost.isEmpty()) {
                healthStatus.put("redis", "CONFIGURED");
                healthStatus.put("redis_host", redisHost);
                healthStatus.put("redis_port", environment.getProperty("spring.data.redis.port", "6379"));
            } else {
                healthStatus.put("redis", "NOT_CONFIGURED");
                healthStatus.put("redis_note", "Redis not configured");
            }
            
            // Always return 200 OK for this endpoint - let individual components report their status
            return ResponseEntity.ok(healthStatus);
            
        } catch (Exception e) {
            // Only return 503 if there's a critical application error
            healthStatus.put("status", "DOWN");
            healthStatus.put("error", e.getMessage());
            healthStatus.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(503).body(healthStatus);
        }
    }

    /**
     * Simple ping endpoint for basic connectivity check.
     * This endpoint should always return OK if the application is running,
     * regardless of external dependencies like database.
     * 
     * @return ResponseEntity with simple OK message
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
    }

    /**
     * Readiness check endpoint for container orchestration.
     * This endpoint should return 200 only when the application is ready to serve traffic.
     * 
     * @return ResponseEntity with readiness status
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readinessCheck() {
        Map<String, Object> readinessStatus = new HashMap<>();
        
        try {
            // Check if database is available and configured
            String dbHost = environment.getProperty("MYSQLHOST");
            if (dbHost != null && !dbHost.isEmpty() && dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(3)) {
                        readinessStatus.put("status", "READY");
                        readinessStatus.put("database", "UP");
                        readinessStatus.put("timestamp", System.currentTimeMillis());
                        return ResponseEntity.ok(readinessStatus);
                    } else {
                        readinessStatus.put("status", "NOT_READY");
                        readinessStatus.put("database", "DOWN");
                        readinessStatus.put("error", "Database connection validation failed");
                        return ResponseEntity.status(503).body(readinessStatus);
                    }
                } catch (Exception e) {
                    readinessStatus.put("status", "NOT_READY");
                    readinessStatus.put("database", "DOWN");
                    readinessStatus.put("error", e.getMessage());
                    return ResponseEntity.status(503).body(readinessStatus);
                }
            } else {
                readinessStatus.put("status", "NOT_READY");
                readinessStatus.put("database", "NOT_CONFIGURED");
                readinessStatus.put("error", "Database environment variables not set");
                return ResponseEntity.status(503).body(readinessStatus);
            }
        } catch (Exception e) {
            readinessStatus.put("status", "NOT_READY");
            readinessStatus.put("error", e.getMessage());
            readinessStatus.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(503).body(readinessStatus);
        }
    }

    /**
     * Database-specific health check endpoint.
     * 
     * @return ResponseEntity with database health status
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> dbStatus = new HashMap<>();
        
        try {
            String dbHost = environment.getProperty("MYSQLHOST");
            if (dbHost != null && !dbHost.isEmpty() && dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(3)) { // Reduced timeout for faster response
                        dbStatus.put("status", "UP");
                        dbStatus.put("host", dbHost);
                        dbStatus.put("port", environment.getProperty("MYSQLPORT", "3306"));
                        dbStatus.put("database", environment.getProperty("MYSQLDATABASE", "yakrooms"));
                        return ResponseEntity.ok(dbStatus);
                    } else {
                        dbStatus.put("status", "DOWN");
                        dbStatus.put("error", "Connection validation failed");
                        return ResponseEntity.status(503).body(dbStatus);
                    }
                } catch (Exception e) {
                    dbStatus.put("status", "DOWN");
                    dbStatus.put("error", e.getMessage());
                    return ResponseEntity.status(503).body(dbStatus);
                }
            } else {
                dbStatus.put("status", "NOT_CONFIGURED");
                dbStatus.put("note", "Database environment variables not set");
                return ResponseEntity.status(503).body(dbStatus);
            }
        } catch (Exception e) {
            dbStatus.put("status", "ERROR");
            dbStatus.put("error", e.getMessage());
            return ResponseEntity.status(503).body(dbStatus);
        }
    }
}
