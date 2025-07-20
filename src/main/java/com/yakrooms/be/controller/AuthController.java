package com.yakrooms.be.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.yakrooms.be.dto.response.JwtLoginResponse;
import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.service.FirebaseService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	FirebaseService firebaseService;

	@PostMapping("/firebase")
	public ResponseEntity<JwtLoginResponse> firebaseLogin(@RequestBody Map<String, String> request) {
		String googleToken = request.get("idToken");

		JwtLoginResponse firebaseUser = firebaseService.verifyTokenAndGetUser(googleToken);

		return ResponseEntity.ok(firebaseUser);
	}
}
