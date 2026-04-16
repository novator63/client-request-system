package com.example.app.comment.controller;

import com.example.app.comment.dto.request.CreateCommentRequest;
import com.example.app.comment.dto.response.CommentResponse;
import com.example.app.comment.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/comments/tickets")
@Validated
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@PostMapping("/{ticketId}")
	@ResponseStatus(CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR') or @ticketAccess.canCommentTicket(#ticketId)")
	public CommentResponse create(
		@PathVariable @Positive(message = "Ticket id must be positive") Long ticketId,
		@Valid @RequestBody CreateCommentRequest request
	) {
		return commentService.create(ticketId, request);
	}

	@GetMapping("/{ticketId}")
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR') or @ticketAccess.canReadTicket(#ticketId)")
	public List<CommentResponse> getByTicketId(
		@PathVariable @Positive(message = "Ticket id must be positive") Long ticketId
	) {
		return commentService.getByTicketId(ticketId);
	}
}
