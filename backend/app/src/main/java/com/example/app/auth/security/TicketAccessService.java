package com.example.app.auth.security;

import com.example.app.ticket.repository.TicketRepository;
import com.example.app.user.entity.UserRole;
import org.springframework.stereotype.Service;

@Service("ticketAccess")
public class TicketAccessService {

	private final TicketRepository ticketRepository;
	private final CurrentUserService currentUserService;

	public TicketAccessService(TicketRepository ticketRepository, CurrentUserService currentUserService) {
		this.ticketRepository = ticketRepository;
		this.currentUserService = currentUserService;
	}

	public boolean canReadTicket(Long ticketId) {
		if (isPrivilegedRole()) {
			return true;
		}

		Long userId = currentUserService.requireCurrentUserId();
		return ticketRepository.existsByIdAndAuthorId(ticketId, userId);
	}

	public boolean canCommentTicket(Long ticketId) {
		return canReadTicket(ticketId);
	}

	private boolean isPrivilegedRole() {
		return currentUserService.hasRole(UserRole.ADMIN)
			|| currentUserService.hasRole(UserRole.OPERATOR);
	}
}
