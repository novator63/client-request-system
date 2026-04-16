package com.example.app.auth.service;

import com.example.app.auth.config.JwtProperties;
import com.example.app.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
	private static final String TYPE_CLAIM = "type";
	private static final String ACCESS_TYPE = "access";
	private static final String REFRESH_TYPE = "refresh";

	private final JwtProperties jwtProperties;
	private final SecretKey signingKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.signingKey = buildSigningKey(jwtProperties.getSecret());
	}

	public String generateAccessToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", user.getRole().name());
		claims.put("userId", user.getId());
		claims.put(TYPE_CLAIM, ACCESS_TYPE);

		return generateToken(user.getEmail(), claims, jwtProperties.getAccessExpiration());
	}

	public String generateRefreshToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(TYPE_CLAIM, REFRESH_TYPE);

		return generateToken(user.getEmail(), claims, jwtProperties.getRefreshExpiration());
	}

	public String extractEmail(String token) {
		return extractClaims(token).getSubject();
	}

	public String extractTokenType(String token) {
		return extractClaims(token).get(TYPE_CLAIM, String.class);
	}

	public Instant extractExpiration(String token) {
		return extractClaims(token).getExpiration().toInstant();
	}

	public boolean isAccessTokenValid(String token) {
		return validateTokenByType(token, ACCESS_TYPE);
	}

	public boolean isRefreshTokenValid(String token) {
		return validateTokenByType(token, REFRESH_TYPE);
	}

	private String generateToken(String subject, Map<String, Object> claims, java.time.Duration expiration) {

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(expiration);

		return Jwts.builder()
			.setClaims(claims)
			.setSubject(subject)
			.setIssuedAt(Date.from(issuedAt))
			.setExpiration(Date.from(expiresAt))
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private boolean validateTokenByType(String token, String expectedType) {
		try {
			Claims claims = extractClaims(token);
			String tokenType = claims.get(TYPE_CLAIM, String.class);
			Date expiration = claims.getExpiration();

			return expectedType.equals(tokenType)
				&& expiration != null
				&& expiration.toInstant().isAfter(Instant.now());
		} catch (ExpiredJwtException exception) {
			return false;
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	private Claims extractClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(signingKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	private SecretKey buildSigningKey(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("JWT secret must be configured");
		}

		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
		}

		try {
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (WeakKeyException exception) {
			throw new IllegalStateException("JWT secret is too weak for HS256", exception);
		}
	}
}