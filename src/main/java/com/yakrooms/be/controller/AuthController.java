package com.yakrooms.be.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakrooms.be.dto.response.JwtLoginResponse;
import com.yakrooms.be.service.FirebaseService;
import com.yakrooms.be.service.RefreshTokenService;
import com.yakrooms.be.util.CookieUtil;
import com.yakrooms.be.security.JwtAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	FirebaseService firebaseService;
	
	@Autowired
	RefreshTokenService refreshTokenService;
	
	@Autowired
	CookieUtil cookieUtil;

	@PostMapping("/firebase")
	public ResponseEntity<JwtLoginResponse> firebaseLogin(@RequestBody Map<String, String> request,
	                                                     HttpServletRequest httpRequest,
	                                                     HttpServletResponse httpResponse) {
		String googleToken = request.get("idToken");

		JwtLoginResponse firebaseUser = firebaseService.verifyTokenAndGetUser(googleToken, httpRequest, httpResponse);

		return ResponseEntity.ok(firebaseUser);
	}
	
	/**
	 * Refresh access token using refresh token cookie
	 * Implements token rotation for enhanced security
	 */
	@PostMapping("/refresh-token")
	public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		try {
			// Get refresh token from cookie
			String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
			if (refreshToken == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "No refresh token found"));
			}
			
			// Extract device info and IP for security tracking
			String deviceInfo = request.getHeader("User-Agent");
			String ipAddress = getClientIpAddress(request);
			
			// Validate and rotate refresh token
			var newRefreshToken = refreshTokenService.validateAndRotateToken(refreshToken, deviceInfo, ipAddress);
			
			// Generate new access token
			String newAccessToken = firebaseService.generateAccessTokenForUser(newRefreshToken.getUserId());
			
			// Set new tokens as secure cookies
			cookieUtil.setAccessTokenCookie(response, newAccessToken, 900); // 15 minutes
			cookieUtil.setRefreshTokenCookie(response, newRefreshToken.getTokenHash(), 604800); // 7 days
			
			return ResponseEntity.ok(Map.of(
				"message", "Token refreshed successfully",
				"accessTokenExpiresIn", 900,
				"refreshTokenExpiresIn", 604800
			));
			
		} catch (SecurityException e) {
			// Clear invalid cookies
			cookieUtil.clearAllTokenCookies(response);
			return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
		}
	}
	
	/**
	 * Logout user and revoke all refresh tokens
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(Authentication authentication, HttpServletResponse response) {
		try {
			if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationDetails) {
				JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
				Long userId = details.getUserId();
				
				// Revoke all refresh tokens for the user
				int revokedCount = refreshTokenService.revokeAllUserTokens(userId);
				
				// Clear all cookies
				cookieUtil.clearAllTokenCookies(response);
				
				return ResponseEntity.ok(Map.of(
					"message", "Logged out successfully",
					"revokedTokens", revokedCount
				));
			}
			
			// Clear cookies even if no authentication context
			cookieUtil.clearAllTokenCookies(response);
			return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
			
		} catch (Exception e) {
			// Always clear cookies on logout attempt
			cookieUtil.clearAllTokenCookies(response);
			return ResponseEntity.status(500).body(Map.of("error", "Logout failed"));
		}
	}
	
	/**
	 * Get client IP address for security tracking
	 */
	private String getClientIpAddress(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}
		
		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}
		
		return request.getRemoteAddr();
	}
}
