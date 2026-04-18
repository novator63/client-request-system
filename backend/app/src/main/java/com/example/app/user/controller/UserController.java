package com.example.app.user.controller;

import com.example.app.user.dto.response.OperatorOptionResponse;
import com.example.app.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/operators")
	@PreAuthorize("hasRole('ADMIN')")
	public List<OperatorOptionResponse> getAvailableOperators() {
		return userService.getAvailableOperators();
	}
}
