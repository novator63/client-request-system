package com.example.app.ticket.service;

import com.example.app.auth.security.CurrentUserService;
import com.example.app.category.entity.Category;
import com.example.app.category.exception.CategoryNotFoundException;
import com.example.app.category.repository.CategoryRepository;
import com.example.app.history.service.HistoryService;
import com.example.app.sla.service.SlaService;
import com.example.app.ticket.dto.request.AssignTicketRequest;
import com.example.app.ticket.dto.request.CreateTicketRequest;
import com.example.app.ticket.dto.request.UpdateTicketClassificationRequest;
import com.example.app.ticket.dto.request.UpdateTicketRequest;
import com.example.app.ticket.dto.request.UpdateTicketStatusRequest;
import com.example.app.ticket.dto.response.TicketListItemResponse;
import com.example.app.ticket.dto.response.TicketResponse;
import com.example.app.ticket.entity.Ticket;
import com.example.app.ticket.entity.TicketStatus;
import com.example.app.ticket.exception.TicketBadRequestException;
import com.example.app.ticket.exception.TicketNotFoundException;
import com.example.app.ticket.mapper.TicketMapper;
import com.example.app.ticket.repository.TicketRepository;
import com.example.app.user.entity.User;
import com.example.app.user.entity.UserRole;
import com.example.app.user.exception.UserNotFoundException;
import com.example.app.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TicketService {

	private final TicketRepository ticketRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;
	private final TicketMapper ticketMapper;
	private final HistoryService historyService;
	private final SlaService slaService;
	private final CurrentUserService currentUserService;

	public TicketService(
		TicketRepository ticketRepository,
		CategoryRepository categoryRepository,
		UserRepository userRepository,
		TicketMapper ticketMapper,
		HistoryService historyService,
		SlaService slaService,
		CurrentUserService currentUserService
	) {
		this.ticketRepository = ticketRepository;
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
		this.ticketMapper = ticketMapper;
		this.historyService = historyService;
		this.slaService = slaService;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public TicketResponse create(CreateTicketRequest request) {
		User author = getCurrentUserEntity();
		Category category = findCategoryById(request.categoryId());

		Ticket ticket = ticketMapper.toEntity(request);
		ticket.setTitle(requireText(ticketMapper.normalizeText(request.title()), "Ticket title is required"));
		ticket.setDescription(requireText(ticketMapper.normalizeText(request.description()), "Ticket description is required"));
		ticket.setStatus(TicketStatus.NEW);
		ticket.setAuthor(author);
		ticket.setAssignee(null);
		ticket.setCategory(category);
		ticket.setClosedAt(null);
		ticket.setDueAt(slaService.calculateDueAt(ticket.getPriority()));

		Ticket savedTicket = ticketRepository.save(ticket);
		historyService.recordTicketCreated(savedTicket, author);
		return ticketMapper.toResponse(savedTicket);
	}

	public List<TicketListItemResponse> getAll() {
		Long currentUserId = currentUserService.requireCurrentUserId();

		List<Ticket> tickets;
		if (currentUserService.hasRole(UserRole.CLIENT)) {
			tickets = ticketRepository.findAllByAuthorIdOrderByCreatedAtDesc(currentUserId);
		} else if (currentUserService.hasRole(UserRole.OPERATOR)) {
			tickets = ticketRepository.findAllByAssigneeIdOrderByCreatedAtDesc(currentUserId);
		} else {
			tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
		}

		return tickets
			.stream()
			.map(ticketMapper::toListItemResponse)
			.map(this::sanitizeListItemForCurrentRole)
			.toList();
	}

	public TicketResponse getById(Long id) {
		return sanitizeTicketResponseForCurrentRole(ticketMapper.toResponse(findTicketById(id)));
	}

	@Transactional
	public TicketResponse update(Long id, UpdateTicketRequest request) {
		Ticket ticket = findTicketById(id);
		ensureCanModifyTicket(ticket);
		ensureTicketIsNotClosed(ticket);

		String title = ticketMapper.normalizeText(request.title());
		String description = ticketMapper.normalizeText(request.description());
		if (title == null && description == null) {
			throw new TicketBadRequestException("At least one field must be provided");
		}

		if (title != null) {
			ticket.setTitle(title);
		}

		if (description != null) {
			ticket.setDescription(description);
		}

		Ticket savedTicket = ticketRepository.save(ticket);
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional
	public TicketResponse assign(Long id, AssignTicketRequest request) {
		User actor = getCurrentUserEntity();
		Ticket ticket = findTicketById(id);
		ensureTicketIsNotClosed(ticket);

		User assignee = userRepository.findById(request.assigneeId())
			.orElseThrow(UserNotFoundException::new);

		TicketStatus previousStatus = ticket.getStatus();
		ticket.setAssignee(assignee);

		if (ticket.getStatus() == TicketStatus.NEW) {
			ticket.setStatus(TicketStatus.IN_PROGRESS);
		}

		Ticket savedTicket = ticketRepository.save(ticket);
		historyService.recordAssigned(savedTicket, actor, assignee);
		if (previousStatus != savedTicket.getStatus()) {
			historyService.recordStatusChanged(savedTicket, actor, previousStatus, savedTicket.getStatus());
		}
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional
	public TicketResponse updateClassification(Long id, UpdateTicketClassificationRequest request) {
		User actor = getCurrentUserEntity();
		Ticket ticket = findTicketById(id);
		ensureTicketIsNotClosed(ticket);

		String oldCategoryName = ticket.getCategory().getName();
		Category category = findCategoryById(request.categoryId());
		ticket.setCategory(category);
		ticket.setPriority(request.priority());
		ticket.setDueAt(slaService.calculateDueAt(ticket.getPriority()));

		Ticket savedTicket = ticketRepository.save(ticket);
		if (!oldCategoryName.equals(savedTicket.getCategory().getName())) {
			historyService.recordCategoryChanged(savedTicket, actor, oldCategoryName, savedTicket.getCategory().getName());
		}
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional
	public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
		User actor = getCurrentUserEntity();
		Ticket ticket = findTicketById(id);
		ensureCanModifyTicket(ticket);
		ensureTicketIsNotClosed(ticket);

		if (request.status() == TicketStatus.CLOSED) {
			throw new TicketBadRequestException("Use POST /tickets/{id}/close to close a ticket");
		}

		TicketStatus oldStatus = ticket.getStatus();
		ticket.setStatus(request.status());
		if (request.status() != TicketStatus.CLOSED) {
			ticket.setClosedAt(null);
		}

		Ticket savedTicket = ticketRepository.save(ticket);
		if (oldStatus != savedTicket.getStatus()) {
			historyService.recordStatusChanged(savedTicket, actor, oldStatus, savedTicket.getStatus());
		}
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional
	public TicketResponse close(Long id) {
		User actor = getCurrentUserEntity();
		Ticket ticket = findTicketById(id);
		ensureCanModifyTicket(ticket);

		if (ticket.getStatus() == TicketStatus.CLOSED) {
			throw new TicketBadRequestException("Ticket is already closed");
		}

		ticket.setStatus(TicketStatus.CLOSED);
		ticket.setClosedAt(LocalDateTime.now());

		Ticket savedTicket = ticketRepository.save(ticket);
		historyService.recordClosed(savedTicket, actor);
		return ticketMapper.toResponse(savedTicket);
	}

	@Transactional
	public void delete(Long id) {
		Ticket ticket = findTicketById(id);
		ticketRepository.delete(ticket);
	}

	private Ticket findTicketById(Long id) {
		return ticketRepository.findById(id)
			.orElseThrow(TicketNotFoundException::new);
	}

	private Category findCategoryById(Long id) {
		return categoryRepository.findById(id)
			.orElseThrow(CategoryNotFoundException::new);
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

	private void ensureTicketIsNotClosed(Ticket ticket) {
		if (ticket.getStatus() == TicketStatus.CLOSED) {
			throw new TicketBadRequestException("Closed ticket cannot be changed");
		}
	}

	private void ensureCanModifyTicket(Ticket ticket) {
		if (currentUserService.hasRole(UserRole.ADMIN)) {
			return;
		}

		if (!currentUserService.hasRole(UserRole.OPERATOR)) {
			throw new AccessDeniedException("Not enough permissions to modify ticket");
		}

		Long currentUserId = currentUserService.requireCurrentUserId();
		Long assigneeId = ticket.getAssignee() != null ? ticket.getAssignee().getId() : null;
		if (assigneeId == null || !assigneeId.equals(currentUserId)) {
			throw new AccessDeniedException("Operator can modify only assigned tickets");
		}
	}

	private TicketListItemResponse sanitizeListItemForCurrentRole(TicketListItemResponse response) {
		if (!currentUserService.hasRole(UserRole.CLIENT)) {
			return response;
		}

		return new TicketListItemResponse(
			response.id(),
			response.title(),
			response.status(),
			null,
			response.authorId(),
			null,
			response.categoryId(),
			response.categoryName(),
			response.createdAt(),
			response.updatedAt(),
			response.dueAt()
		);
	}

	private TicketResponse sanitizeTicketResponseForCurrentRole(TicketResponse response) {
		if (!currentUserService.hasRole(UserRole.CLIENT)) {
			return response;
		}

		return new TicketResponse(
			response.id(),
			response.title(),
			response.description(),
			response.status(),
			null,
			response.authorId(),
			response.authorName(),
			null,
			null,
			response.categoryId(),
			response.categoryName(),
			response.createdAt(),
			response.updatedAt(),
			response.closedAt(),
			response.dueAt()
		);
	}
}
