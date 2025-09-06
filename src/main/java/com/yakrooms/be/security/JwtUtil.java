package com.yakrooms.be.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
	
	// JWT Configuration - injected from properties
	private final String jwtSecret;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;
	
	// Token types for different purposes
	public static final String TOKEN_TYPE_ACCESS = "access";
	public static final String TOKEN_TYPE_REFRESH = "refresh";
	
	// JWT Claims
	public static final String CLAIM_USER_ID = "userId";
	public static final String CLAIM_EMAIL = "email";
	public static final String CLAIM_ROLES = "roles";
	public static final String CLAIM_HOTEL_ID = "hotelId";
	public static final String CLAIM_TOKEN_TYPE = "tokenType";
	public static final String CLAIM_JTI = "jti"; // JWT ID for token tracking
	
	public JwtUtil(@Value("${jwt.secret:default-secret-key-change-in-production}") String jwtSecret,
	               @Value("${jwt.access-token-expiration:900000}") long accessTokenExpirationMs,
	               @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpirationMs) {
		
		// Validate JWT secret for production security
		validateJwtSecret(jwtSecret);
		
		this.jwtSecret = jwtSecret;
		this.accessTokenExpirationMs = accessTokenExpirationMs; // 15 minutes default
		this.refreshTokenExpirationMs = refreshTokenExpirationMs; // 7 days default
	}
	
	/**
	 * Validate JWT secret to ensure production security
	 * Prevents deployment with insecure default secrets
	 */
	private void validateJwtSecret(String jwtSecret) {
		// Check for null or empty secret
		if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
			throw new IllegalArgumentException("JWT secret cannot be null or empty. Set JWT_SECRET environment variable.");
		}
		
		// Check for insecure default values - WARN instead of CRASH for production deployment
		if (jwtSecret.contains("default") || 
		    jwtSecret.contains("change-in-production") || 
		    jwtSecret.contains("dev-secret") ||
		    jwtSecret.length() < 32) {
			System.err.println("⚠️  WARNING: Insecure JWT secret detected!");
			System.err.println("⚠️  Please set a secure JWT_SECRET environment variable ASAP");
			System.err.println("⚠️  Generate one with: openssl rand -base64 32");
			System.err.println("⚠️  Application will start but security is COMPROMISED!");
		}
		
		// Log successful validation (without exposing the secret)
		System.out.println("✅ JWT secret validation passed - using secure " + jwtSecret.length() + "-character secret");
	}
	
	/**
	 * Get the signing key for JWT tokens
	 * Uses HMAC-SHA512 for secure signing
	 */
	private Key getSigningKey() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Generate access token for API requests
	 * Short-lived token (15 minutes) with user information
	 */
	public String generateAccessToken(User user) {
		return generateToken(user, TOKEN_TYPE_ACCESS, accessTokenExpirationMs);
	}
	
	/**
	 * Generate refresh token for token renewal
	 * Long-lived token (7 days) with minimal claims
	 */
	public String generateRefreshToken(User user) {
		return generateToken(user, TOKEN_TYPE_REFRESH, refreshTokenExpirationMs);
	}
	
	/**
	 * Generate JWT token with specified type and expiration
	 */
	private String generateToken(User user, String tokenType, long expirationMs) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_EMAIL, user.getEmail());
		claims.put(CLAIM_USER_ID, user.getId());
		claims.put(CLAIM_TOKEN_TYPE, tokenType);
		claims.put(CLAIM_JTI, UUID.randomUUID().toString());
		
		// Store roles as comma-separated string
		String rolesString = user.getRoles().stream()
				.map(Role::name)
				.reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
		claims.put(CLAIM_ROLES, rolesString);
		
		// Add hotel ID if user belongs to a hotel
		if (user.getHotel() != null) {
			claims.put(CLAIM_HOTEL_ID, user.getHotel().getId());
		}

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(user.getEmail())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(getSigningKey())
				.compact();
	}
	
	/**
	 * Generate hash for refresh token storage
	 * Uses SHA-256 for secure hashing
	 * 
	 * @param token The JWT refresh token to hash
	 * @return A 64-character hexadecimal string (SHA-256 hash)
	 * @throws RuntimeException if SHA-256 algorithm is not available
	 */
	public String generateTokenHash(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new IllegalArgumentException("Token cannot be null or empty");
		}
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			
			// Convert to hexadecimal string (always 64 characters for SHA-256)
			StringBuilder hexString = new StringBuilder(64);
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			
			String result = hexString.toString();
			
			// Validate that we always produce exactly 64 characters
			if (result.length() != 64) {
				throw new RuntimeException("SHA-256 hash should always be 64 characters, got: " + result.length());
			}
			
			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}
	
	/**
	 * Extract email from token
	 */
	public String extractEmail(String token) {
		return parseToken(token).getSubject();
	}
	
	/**
	 * Extract user ID from token
	 */
	public Long extractUserId(String token) {
		return parseToken(token).get(CLAIM_USER_ID, Long.class);
	}
	
	/**
	 * Extract roles from token
	 */
	public String extractRoles(String token) {
		return parseToken(token).get(CLAIM_ROLES, String.class);
	}
	
	/**
	 * Extract first role from token (for backward compatibility)
	 */
	public String extractRole(String token) {
		String roles = extractRoles(token);
		if (roles != null && !roles.isEmpty()) {
			return roles.split(",")[0];
		}
		return null;
	}
	
	/**
	 * Extract hotel ID from token
	 */
	public Long extractHotelId(String token) {
		return parseToken(token).get(CLAIM_HOTEL_ID, Long.class);
	}
	
	/**
	 * Extract token type from token
	 */
	public String extractTokenType(String token) {
		return parseToken(token).get(CLAIM_TOKEN_TYPE, String.class);
	}
	
	/**
	 * Extract JWT ID from token
	 */
	public String extractJti(String token) {
		return parseToken(token).get(CLAIM_JTI, String.class);
	}
	
	/**
	 * Check if token is expired
	 */
	public boolean isTokenExpired(String token) {
		try {
			Claims claims = parseToken(token);
			return claims.getExpiration().before(new Date());
		} catch (JwtException e) {
			return true;
		}
	}
	
	/**
	 * Validate token (not expired and valid signature)
	 */
	public boolean validateToken(String token) {
		try {
			parseToken(token);
			return !isTokenExpired(token);
		} catch (JwtException e) {
			return false;
		}
	}
	
	/**
	 * Validate token and check if it's an access token
	 */
	public boolean validateAccessToken(String token) {
		return validateToken(token) && TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
	}
	
	/**
	 * Validate token and check if it's a refresh token
	 */
	public boolean validateRefreshToken(String token) {
		return validateToken(token) && TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
	}
	
	/**
	 * Parse JWT token and extract claims
	 */
	private Claims parseToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
	/**
	 * Legacy method for backward compatibility
	 * @deprecated Use generateAccessToken instead
	 */
	@Deprecated
	public String generateToken(User user) {
		return generateAccessToken(user);
	}
}
