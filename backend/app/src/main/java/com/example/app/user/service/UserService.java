package com.example.app.user.service;

import com.example.app.auth.security.CurrentUserService;
import com.example.app.user.dto.response.UserResponse;
import com.example.app.user.entity.User;
import com.example.app.user.exception.UserNotFoundException;
import com.example.app.user.mapper.UserMapper;
import com.example.app.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final CurrentUserService currentUserService;

	public UserService(UserRepository userRepository, UserMapper userMapper, CurrentUserService currentUserService) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.currentUserService = currentUserService;
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
		return userMapper.toResponse(currentUserService.requireCurrentUser());
	}
}
