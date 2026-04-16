package com.example.app.auth.security;

import com.example.app.auth.exception.UnauthenticatedException;
import com.example.app.user.entity.User;
import com.example.app.user.entity.UserRole;
import com.example.app.user.exception.UserNotFoundException;
import com.example.app.user.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User requireCurrentUser() {
		String email = requireAuthenticatedEmail();
		return userRepository.findByEmail(email)
			.orElseThrow(UserNotFoundException::new);
	}

	public Long requireCurrentUserId() {
		return requireCurrentUser().getId();
	}

	public boolean hasRole(UserRole role) {
		return requireCurrentUser().getRole() == role;
	}

	public String requireAuthenticatedEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
			|| !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			throw new UnauthenticatedException();
		}

		return authentication.getName();
	}
}
