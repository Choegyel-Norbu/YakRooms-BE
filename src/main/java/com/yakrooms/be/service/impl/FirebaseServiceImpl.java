package com.yakrooms.be.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.yakrooms.be.dto.mapper.UserMapper;
import com.yakrooms.be.dto.response.JwtLoginResponse;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.security.JwtUtil;
import com.yakrooms.be.service.FirebaseService;
import com.yakrooms.be.service.RefreshTokenService;
import com.yakrooms.be.util.FirebaseUserData;
import com.yakrooms.be.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FirebaseServiceImpl implements FirebaseService {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	RefreshTokenService refreshTokenService;
	
	@Autowired
	CookieUtil cookieUtil;

	@Override
	public JwtLoginResponse verifyTokenAndGetUser(String idToken) {
		try {
			FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
			String uid = decoded.getUid();
			String name = (String) decoded.getClaims().getOrDefault("name", "Guest");
			String email = decoded.getEmail();
			String picture = (String) decoded.getClaims().get("picture");

			FirebaseUserData data = new FirebaseUserData();
			data.setUid(uid);
			data.setEmail(email);
			data.setName(name);
			data.setProfilePictureUrl(picture);
			return handleUser(data);
			
		} catch (FirebaseAuthException e) {
			throw new RuntimeException("Invalid Firebase ID token", e);
		}
	}
	
	@Override
	public JwtLoginResponse verifyTokenAndGetUser(String idToken, HttpServletRequest request, HttpServletResponse response) {
		try {
			FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
			String uid = decoded.getUid();
			String name = (String) decoded.getClaims().getOrDefault("name", "Guest");
			String email = decoded.getEmail();
			String picture = (String) decoded.getClaims().get("picture");

			FirebaseUserData data = new FirebaseUserData();
			data.setUid(uid);
			data.setEmail(email);
			data.setName(name);
			data.setProfilePictureUrl(picture);
			return handleUserWithCookies(data, request, response);
			
		} catch (FirebaseAuthException e) {
			throw new RuntimeException("Invalid Firebase ID token", e);
		}
	}
	
	@Override
	public String generateAccessTokenForUser(Long userId) {
		Optional<User> userOpt = userRepo.findById(userId);
		if (userOpt.isEmpty()) {
			throw new RuntimeException("User not found");
		}
		return jwtUtil.generateAccessToken(userOpt.get());
	}

	@Transactional
	private JwtLoginResponse handleUser(FirebaseUserData data) {
	    Optional<User> optionalUser = userRepo.findByEmailWithCollections(data.getEmail());
	    User user;

	    if (optionalUser.isPresent()) {
	        user = optionalUser.get();

	        boolean updated = false;
	        if (data.getProfilePictureUrl() != null && !data.getProfilePictureUrl().equals(user.getProfilePicUrl())) {
	            user.setProfilePicUrl(data.getProfilePictureUrl());
	            updated = true;
	        }
	        if (data.getName() != null && !data.getName().equals(user.getName())) {
	            user.setName(data.getName());
	            updated = true;
	        }
	        if (updated) {
	            user.setUpdatedAt(LocalDateTime.now());
	            user.addRole(Role.GUEST);
	            user = userRepo.save(user);
	        }

	    } else {
	        user = new User();
	        user.setEmail(data.getEmail());
	        user.setName(data.getName());
	        user.addRole(Role.GUEST);
	        user.setProfilePicUrl(data.getProfilePictureUrl());
	        user.setCreatedAt(LocalDateTime.now());
	        user.setUpdatedAt(LocalDateTime.now());

	        user = userRepo.save(user);
	    }

	    // Note: This method is deprecated - use handleUserWithCookies for secure token handling
	    // Tokens should not be exposed in response body for security reasons
	    String token = jwtUtil.generateAccessToken(user);

	    return new JwtLoginResponse(token, UserMapper.toUserResponse(user));
	}
	
	@Transactional
	private JwtLoginResponse handleUserWithCookies(FirebaseUserData data, HttpServletRequest request, HttpServletResponse response) {
	    Optional<User> optionalUser = userRepo.findByEmailWithCollections(data.getEmail());
	    User user;

	    if (optionalUser.isPresent()) {
	        user = optionalUser.get();

	        boolean updated = false;
	        if (data.getProfilePictureUrl() != null && !data.getProfilePictureUrl().equals(user.getProfilePicUrl())) {
	            user.setProfilePicUrl(data.getProfilePictureUrl());
	            updated = true;
	        }
	        if (data.getName() != null && !data.getName().equals(user.getName())) {
	            user.setName(data.getName());
	            updated = true;
	        }
	        if (updated) {
	            user.setUpdatedAt(LocalDateTime.now());
	            user.addRole(Role.GUEST);
	            user = userRepo.save(user);
	        }

	    } else {
	        user = new User();
	        user.setEmail(data.getEmail());
	        user.setName(data.getName());
	        user.addRole(Role.GUEST);
	        user.setProfilePicUrl(data.getProfilePictureUrl());
	        user.setCreatedAt(LocalDateTime.now());
	        user.setUpdatedAt(LocalDateTime.now());

	        user = userRepo.save(user);
	    }

	    // Generate access token (15 minutes)
	    String accessToken = jwtUtil.generateAccessToken(user);
	    
	    // Generate refresh token (7 days)
	    String deviceInfo = request.getHeader("User-Agent");
	    String ipAddress = getClientIpAddress(request);
	    var refreshToken = refreshTokenService.createRefreshToken(user, deviceInfo, ipAddress);
	    
	    // Set secure cookies
	    cookieUtil.setAccessTokenCookie(response, accessToken, 900); // 15 minutes
	    cookieUtil.setRefreshTokenCookie(response, refreshToken.getToken(), 604800); // 7 days
	 
	    // Return response WITHOUT access token for security
	    // Token is now only available via secure HTTP-only cookie
	    return new JwtLoginResponse(UserMapper.toUserResponse(user));
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