package com.yakrooms.be.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.yakrooms.be.dto.mapper.UserMapper;
import com.yakrooms.be.dto.response.JwtLoginResponse;
import com.yakrooms.be.dto.response.UserResponse;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.security.JwtUtil;
import com.yakrooms.be.service.FirebaseService;
import com.yakrooms.be.util.FirebaseUserData;

@Component
public class FirebaseServiceImpl implements FirebaseService {
	
	@Autowired
	UserRepository userRepo;
	
	private JwtUtil jwtUtil;

	@Override
	public JwtLoginResponse verifyTokenAndGetUser(String idToken) {
		try {
			FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
			String uid = decoded.getUid();
			String name = (String) decoded.getClaims().getOrDefault("name", "Guest");
			String email = decoded.getEmail();

			FirebaseUserData data = new FirebaseUserData();
			data.setUid(uid);
			data.setEmail(email);
			data.setName(name);
			return handleUser(data);
		} catch (FirebaseAuthException e) {
			throw new RuntimeException("Invalid Firebase ID token", e);
		}
	}

	private JwtLoginResponse handleUser(FirebaseUserData data) {
	    Optional<User> optionalUser = userRepo.findByEmail(data.getEmail());
	    User user;

	    if (optionalUser.isPresent()) {
	        user = optionalUser.get();
	    } else {
	        user = new User();
	        user.setEmail(data.getEmail());
	        user.setName(data.getName());
	        user.setRole(Role.GUEST);
	        user.setCreatedAt(LocalDateTime.now());
	        user.setUpdatedAt(LocalDateTime.now());

	        user = userRepo.save(user);
	    }
		User newUser = new User();
	    newUser.setEmail(data.getEmail());
	    newUser.setName(data.getName());
	    newUser.setRole(Role.GUEST); 
	    newUser.setCreatedAt(LocalDateTime.now());
	    newUser.setUpdatedAt(LocalDateTime.now());
		
	    String token = jwtUtil.generateToken(user);

	    return new JwtLoginResponse(token, UserMapper.toUserResponse(user));
	}

}
