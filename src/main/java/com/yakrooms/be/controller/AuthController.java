package com.yakrooms.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakrooms.be.dto.response.JwtLoginResponse;
import com.yakrooms.be.service.FirebaseService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	FirebaseService firebaseService;

	@PostMapping("/firebase")
	public ResponseEntity<JwtLoginResponse> firebaseLogin(@RequestHeader("Authorization") String authHeader) {
		String idToken = authHeader.replace("Bearer ", "");

		JwtLoginResponse firebaseUser = firebaseService.verifyTokenAndGetUser(idToken);

		return ResponseEntity.ok(firebaseUser);
	}

}
