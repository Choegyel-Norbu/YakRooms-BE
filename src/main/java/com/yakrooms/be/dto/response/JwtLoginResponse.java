package com.yakrooms.be.dto.response;

/**
 * Secure login response that does NOT expose tokens in response body
 * Tokens are stored in secure HTTP-only cookies for enhanced security
 */
public class JwtLoginResponse {
	private String token; // Will be null for security - token in cookies only
	private UserResponse user;
	private boolean success;
	private String message;

	public JwtLoginResponse(String token, UserResponse user) {
		this.token = token;
		this.user = user;
		this.success = true;
		this.message = "Login successful";
	}

	public JwtLoginResponse(UserResponse user) {
		this.token = null; // No token in response body for security
		this.user = user;
		this.success = true;
		this.message = "Login successful - tokens stored in secure cookies";
	}

	public String getToken() {
		return token; // Will be null for security
	}

	public UserResponse getUser() {
		return user;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
}
