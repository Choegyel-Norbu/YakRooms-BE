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
     * 
     * @return ResponseEntity with detailed health status
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Basic application status
            healthStatus.put("status", "UP");
            healthStatus.put("application", "YakRooms Backend");
            healthStatus.put("timestamp", System.currentTimeMillis());
            
            // Check database configuration and connectivity
            String dbHost = environment.getProperty("MYSQLHOST");
            if (dbHost != null && !dbHost.isEmpty() && dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(5)) {
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
            
            // Check Redis configuration
            String redisHost = environment.getProperty("REDIS_HOST");
            if (redisHost != null && !redisHost.isEmpty()) {
                healthStatus.put("redis", "CONFIGURED");
            } else {
                healthStatus.put("redis", "NOT_CONFIGURED");
                healthStatus.put("redis_note", "Redis not configured");
            }
            
            return ResponseEntity.ok(healthStatus);
            
        } catch (Exception e) {
            healthStatus.put("status", "DOWN");
            healthStatus.put("error", e.getMessage());
            return ResponseEntity.status(503).body(healthStatus);
        }
    }

    /**
     * Simple ping endpoint for basic connectivity check.
     * 
     * @return ResponseEntity with simple OK message
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
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
                    if (connection.isValid(5)) {
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
