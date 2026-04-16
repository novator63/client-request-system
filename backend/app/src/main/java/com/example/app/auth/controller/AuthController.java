package com.example.app.auth.controller;

import com.example.app.auth.dto.request.LoginRequest;
import com.example.app.auth.dto.response.AuthResponse;
import com.example.app.auth.dto.response.LoginResponse;
import com.example.app.auth.dto.response.RefreshResponse;
import com.example.app.auth.service.AuthService;
import com.example.app.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		return authService.login(request, response);
	}

	@PostMapping("/refresh")
	public RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
		return authService.refresh(request, response);
	}

	@PostMapping("/logout")
	public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {
		return authService.logout(request, response);
	}

	@GetMapping("/me")
	public UserResponse me() {
		return authService.getCurrentUser();
	}
}