package com.example.app.user.mapper;

import com.example.app.user.dto.response.UserListItemResponse;
import com.example.app.user.dto.response.OperatorOptionResponse;
import com.example.app.user.dto.response.UserResponse;
import com.example.app.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getFullName(),
			user.getRole(),
			user.isActive()
		);
	}

	public UserListItemResponse toListItemResponse(User user) {
		return new UserListItemResponse(
			user.getId(),
			user.getEmail(),
			user.getFullName(),
			user.getRole()
		);
	}

	public OperatorOptionResponse toOperatorOptionResponse(User user, long activeTicketsCount, long overdueTicketsCount) {
		return new OperatorOptionResponse(
			user.getId(),
			user.getFullName(),
			user.getEmail(),
			activeTicketsCount,
			overdueTicketsCount
		);
	}
}
