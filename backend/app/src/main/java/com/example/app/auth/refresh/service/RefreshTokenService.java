package com.example.app.auth.refresh.service;

import com.example.app.auth.exception.InvalidRefreshTokenException;
import com.example.app.auth.refresh.entity.RefreshToken;
import com.example.app.auth.refresh.repository.RefreshTokenRepository;
import com.example.app.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Transactional
	public RefreshToken create(User user, String token, Instant expiresAt) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(token);
		refreshToken.setExpiresAt(expiresAt);
		refreshToken.setRevoked(false);
		return refreshTokenRepository.save(refreshToken);
	}

	@Transactional(readOnly = true)
	public RefreshToken requireActiveToken(String token) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
			.orElseThrow(InvalidRefreshTokenException::new);

		if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
			throw new InvalidRefreshTokenException();
		}

		return refreshToken;
	}

	@Transactional
	public void revoke(RefreshToken refreshToken) {
		refreshToken.setRevoked(true);
	}

	@Transactional
	public void revokeIfExists(String token) {
		refreshTokenRepository.findByTokenAndRevokedFalse(token)
			.ifPresent(existingToken -> existingToken.setRevoked(true));
	}
}
