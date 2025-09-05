package com.yakrooms.be.service;

import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.response.JwtLoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface FirebaseService {
	
	public JwtLoginResponse verifyTokenAndGetUser(String idToken);
	
	public JwtLoginResponse verifyTokenAndGetUser(String idToken, HttpServletRequest request, HttpServletResponse response);
	
	public String generateAccessTokenForUser(Long userId);

}
