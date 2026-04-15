package com.example.app.user.service;

import com.example.app.auth.exception.UnauthenticatedException;
import com.example.app.user.dto.response.UserResponse;
import com.example.app.user.entity.User;
import com.example.app.user.exception.UserNotFoundException;
import com.example.app.user.mapper.UserMapper;
import com.example.app.user.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public UserResponse findByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(UserNotFoundException::new);
		return userMapper.toResponse(user);
	}

	public UserResponse getById(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(UserNotFoundException::new);
		return userMapper.toResponse(user);
	}

	public UserResponse getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
			|| !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			throw new UnauthenticatedException();
		}

		String email = authentication.getName();
		User user = userRepository.findByEmail(email)
			.orElseThrow(UserNotFoundException::new);

		return userMapper.toResponse(user);
	}
}
