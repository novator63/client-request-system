package com.example.app.auth.dto.response;

import java.time.Instant;

public record LoginResponse(
	String token,
	String tokenType,
	Instant expiresAt
) {
}