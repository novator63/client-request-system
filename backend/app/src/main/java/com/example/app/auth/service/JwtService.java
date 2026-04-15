package com.example.app.auth.service;

import com.example.app.auth.config.JwtProperties;
import com.example.app.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

	private final JwtProperties jwtProperties;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", user.getRole().name());
		claims.put("userId", user.getId());

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(jwtProperties.getExpiration());

		return Jwts.builder()
			.setClaims(claims)
			.setSubject(user.getEmail())
			.setIssuedAt(Date.from(issuedAt))
			.setExpiration(Date.from(expiresAt))
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String extractEmail(String token) {
		return extractClaims(token).getSubject();
	}

	public Instant extractExpiration(String token) {
		return extractClaims(token).getExpiration().toInstant();
	}

	public boolean isTokenValid(String token) {
		try {
			extractClaims(token);
			return true;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private Claims extractClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}