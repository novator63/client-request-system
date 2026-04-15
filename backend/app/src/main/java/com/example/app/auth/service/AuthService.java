package com.example.app.auth.service;

import com.example.app.auth.dto.request.LoginRequest;
import com.example.app.auth.dto.response.LoginResponse;
import com.example.app.auth.exception.InvalidCredentialsException;
import com.example.app.user.dto.response.UserResponse;
import com.example.app.user.entity.User;
import com.example.app.user.repository.UserRepository;
import com.example.app.user.service.UserService;
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

	public AuthService(UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		UserService userService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userService = userService;
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		String token = jwtService.generateToken(user);
		Instant expiresAt = jwtService.extractExpiration(token);

		return new LoginResponse(token, "Bearer", expiresAt);
	}

	public UserResponse getCurrentUser() {
		return userService.getCurrentUser();
	}
}