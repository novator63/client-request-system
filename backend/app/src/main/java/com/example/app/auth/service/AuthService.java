package com.example.app.auth.service;

import com.example.app.auth.dto.request.LoginRequest;
import com.example.app.auth.dto.response.AuthResponse;
import com.example.app.auth.dto.response.LoginResponse;
import com.example.app.auth.dto.response.RefreshResponse;
import com.example.app.auth.exception.InvalidRefreshTokenException;
import com.example.app.auth.exception.InvalidCredentialsException;
import com.example.app.auth.cookie.RefreshTokenCookieService;
import com.example.app.auth.refresh.entity.RefreshToken;
import com.example.app.auth.refresh.service.RefreshTokenService;
import com.example.app.user.dto.response.UserResponse;
import com.example.app.user.entity.User;
import com.example.app.user.repository.UserRepository;
import com.example.app.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserService userService;
	private final RefreshTokenService refreshTokenService;
	private final RefreshTokenCookieService refreshTokenCookieService;

	public AuthService(UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		UserService userService,
		RefreshTokenService refreshTokenService,
		RefreshTokenCookieService refreshTokenCookieService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userService = userService;
		this.refreshTokenService = refreshTokenService;
		this.refreshTokenCookieService = refreshTokenCookieService;
	}

	@Transactional
	public LoginResponse login(LoginRequest request, HttpServletResponse response) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(InvalidCredentialsException::new);

		if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		String accessToken = jwtService.generateAccessToken(user);
		Instant accessExpiresAt = jwtService.extractExpiration(accessToken);

		String refreshToken = jwtService.generateRefreshToken(user);
		Instant refreshExpiresAt = jwtService.extractExpiration(refreshToken);
		refreshTokenService.create(user, refreshToken, refreshExpiresAt);

		// Добавляем cookie с refresh токеном в ответ, который будет отправляться клиенту при успешной аутентификации
		refreshTokenCookieService.addRefreshTokenCookie(response, refreshToken);

		return new LoginResponse(accessToken, "Bearer", accessExpiresAt);
	}

	@Transactional
	public RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = refreshTokenCookieService.extractRefreshToken(request)
			.orElseThrow(InvalidRefreshTokenException::new);

		if (!jwtService.isRefreshTokenValid(refreshToken)) {
			throw new InvalidRefreshTokenException();
		}

		String email;
		try {
			email = jwtService.extractEmail(refreshToken);
		} catch (JwtException | IllegalArgumentException exception) {
			throw new InvalidRefreshTokenException();
		}

		User user = userRepository.findByEmail(email)
			.orElseThrow(InvalidRefreshTokenException::new);

		if (!user.isActive()) {
			refreshTokenService.revokeIfExists(refreshToken);
			refreshTokenCookieService.clearRefreshTokenCookie(response);
			throw new InvalidRefreshTokenException();
		}

		RefreshToken storedRefreshToken = refreshTokenService.requireActiveToken(refreshToken);
		if (!storedRefreshToken.getUser().getId().equals(user.getId())) {
			throw new InvalidRefreshTokenException();
		}

		refreshTokenService.revoke(storedRefreshToken);

		String newRefreshToken = jwtService.generateRefreshToken(user);
		Instant newRefreshExpiresAt = jwtService.extractExpiration(newRefreshToken);
		refreshTokenService.create(user, newRefreshToken, newRefreshExpiresAt);
		refreshTokenCookieService.addRefreshTokenCookie(response, newRefreshToken);

		String newAccessToken = jwtService.generateAccessToken(user);
		Instant accessExpiresAt = jwtService.extractExpiration(newAccessToken);

		return new RefreshResponse(newAccessToken, "Bearer", accessExpiresAt);
	}

	@Transactional
	public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {
		refreshTokenCookieService.extractRefreshToken(request)
			.ifPresent(refreshTokenService::revokeIfExists);

		refreshTokenCookieService.clearRefreshTokenCookie(response);
		return new AuthResponse("Logged out");
	}

	public UserResponse getCurrentUser() {
		return userService.getCurrentUser();
	}
}