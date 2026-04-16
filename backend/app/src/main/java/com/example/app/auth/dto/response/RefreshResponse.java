package com.example.app.auth.dto.response;

import java.time.Instant;

public record RefreshResponse(
	String accessToken,
	String tokenType,
	Instant expiresAt
) {
}
