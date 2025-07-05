package com.yakrooms.be.dto.response;

public class JwtLoginResponse {
	private String token;
	private UserResponse user;

	public JwtLoginResponse(String token, UserResponse user) {
		this.token = token;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public UserResponse getUser() {
		return user;
	}
}
