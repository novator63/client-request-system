package com.example.app.comment.mapper;

import com.example.app.comment.dto.request.CreateCommentRequest;
import com.example.app.comment.dto.response.CommentResponse;
import com.example.app.comment.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

	public Comment toEntity(CreateCommentRequest request) {
		Comment comment = new Comment();
		comment.setContent(normalizeText(request.content()));
		return comment;
	}

	public CommentResponse toResponse(Comment comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getTicket().getId(),
			comment.getAuthor().getId(),
			comment.getAuthor().getFullName(),
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}

	public String normalizeText(String value) {
		if (value == null) {
			return null;
		}

		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
