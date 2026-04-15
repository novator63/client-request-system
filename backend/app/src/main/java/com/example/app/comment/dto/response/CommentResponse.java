package com.example.app.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
	Long id,
	Long ticketId,
	Long authorId,
	String authorFullName,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
