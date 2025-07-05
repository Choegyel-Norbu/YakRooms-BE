package com.yakrooms.be.service;

import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseToken;
import com.yakrooms.be.dto.response.JwtLoginResponse;
import com.yakrooms.be.dto.response.UserResponse;
import com.yakrooms.be.util.FirebaseUserData;

@Service
public interface FirebaseService {
	
	public JwtLoginResponse verifyTokenAndGetUser(String idToken);
	

}
