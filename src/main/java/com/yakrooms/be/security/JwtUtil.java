package com.yakrooms.be.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.yakrooms.be.model.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JwtUtil {
	private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Generates a secure 512-bit key

	private static final long EXPIRATION_TIME = 864_000_000; // 10 days

	public static String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
	    claims.put("email", user.getEmail());
	    claims.put("role", user.getRole().name());
	    claims.put("userId", user.getId());
	    claims.put("hotelId", user.getHotel() != null ? user.getHotel().getId() : null);

	    return Jwts.builder()
	            .setClaims(claims)
	            .setSubject(user.getEmail())
	            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
	            .signWith(key)
	            .compact();
	}

	public static String extractEmail(String token) {
		return parseToken(token).getSubject();
	}

	public static Long extractUserId(String token) {
		return parseToken(token).get("userId", Long.class);
	}

	public static String extractRole(String token) {
		return parseToken(token).get("role", String.class);
	}

	public static Long extractHotelId(String token) {
		return parseToken(token).get("hotelId", Long.class);
	}

	public static boolean validateToken(String token) {
		try {
			parseToken(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	private static Claims parseToken(String token) {
		return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
	}
}
