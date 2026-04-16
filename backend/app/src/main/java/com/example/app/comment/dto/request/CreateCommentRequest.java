package com.example.app.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
	@NotBlank(message = "Comment text is required")
	@Size(max = 500, message = "Comment text must be at most 500 characters")
	String content
) {
}
