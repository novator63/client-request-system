package com.example.app.user.dto.response;

public record OperatorOptionResponse(
	Long id,
	String fullName,
	String email,
	Long activeTicketsCount,
	Long overdueTicketsCount
) {
}
