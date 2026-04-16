package com.example.app.comment.service;

import com.example.app.auth.security.CurrentUserService;
import com.example.app.comment.dto.request.CreateCommentRequest;
import com.example.app.comment.dto.response.CommentResponse;
import com.example.app.comment.entity.Comment;
import com.example.app.comment.mapper.CommentMapper;
import com.example.app.comment.repository.CommentRepository;
import com.example.app.history.service.HistoryService;
import com.example.app.ticket.entity.Ticket;
import com.example.app.ticket.exception.TicketBadRequestException;
import com.example.app.ticket.exception.TicketNotFoundException;
import com.example.app.ticket.repository.TicketRepository;
import com.example.app.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;
	private final TicketRepository ticketRepository;
	private final CommentMapper commentMapper;
	private final HistoryService historyService;
	private final CurrentUserService currentUserService;

	public CommentService(
		CommentRepository commentRepository,
		TicketRepository ticketRepository,
		CommentMapper commentMapper,
		HistoryService historyService,
		CurrentUserService currentUserService
	) {
		this.commentRepository = commentRepository;
		this.ticketRepository = ticketRepository;
		this.commentMapper = commentMapper;
		this.historyService = historyService;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public CommentResponse create(Long ticketId, CreateCommentRequest request) {
		Ticket ticket = findTicketById(ticketId);
		User author = getCurrentUserEntity();

		Comment comment = commentMapper.toEntity(request);
		comment.setContent(requireText(commentMapper.normalizeText(request.content()), "Comment text is required"));
		comment.setTicket(ticket);
		comment.setAuthor(author);

		Comment savedComment = commentRepository.save(comment);
		historyService.recordCommentAdded(ticket, author);
		return commentMapper.toResponse(savedComment);
	}

	public List<CommentResponse> getByTicketId(Long ticketId) {
		findTicketById(ticketId);

		return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
			.stream()
			.map(commentMapper::toResponse)
			.toList();
	}

	private Ticket findTicketById(Long id) {
		return ticketRepository.findById(id)
			.orElseThrow(TicketNotFoundException::new);
	}

	private User getCurrentUserEntity() {
		return currentUserService.requireCurrentUser();
	}

	private String requireText(String value, String message) {
		if (value == null) {
			throw new TicketBadRequestException(message);
		}

		return value;
	}
}
