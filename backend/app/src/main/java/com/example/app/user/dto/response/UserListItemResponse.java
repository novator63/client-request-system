package com.example.app.user.dto.response;

import com.example.app.user.entity.UserRole;

public record UserListItemResponse(
	Long id,
	String email,
	String fullName,
	UserRole role
) {
}
