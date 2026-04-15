package com.example.app.ticket.mapper;

import com.example.app.ticket.dto.request.CreateTicketRequest;
import com.example.app.ticket.dto.response.TicketListItemResponse;
import com.example.app.ticket.dto.response.TicketResponse;
import com.example.app.ticket.entity.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

	public Ticket toEntity(CreateTicketRequest request) {
		Ticket ticket = new Ticket();
		ticket.setTitle(normalizeText(request.title()));
		ticket.setDescription(normalizeText(request.description()));
		ticket.setPriority(request.priority());
		return ticket;
	}

	public TicketResponse toResponse(Ticket ticket) {
		return new TicketResponse(
			ticket.getId(),
			ticket.getTitle(),
			ticket.getDescription(),
			ticket.getStatus(),
			ticket.getPriority(),
			ticket.getAuthor().getId(),
			ticket.getAuthor().getFullName(),
			ticket.getAssignee() != null ? ticket.getAssignee().getId() : null,
			ticket.getAssignee() != null ? ticket.getAssignee().getFullName() : null,
			ticket.getCategory().getId(),
			ticket.getCategory().getName(),
			ticket.getCreatedAt(),
			ticket.getUpdatedAt(),
			ticket.getClosedAt(),
			ticket.getDueAt()
		);
	}

	public TicketListItemResponse toListItemResponse(Ticket ticket) {
		return new TicketListItemResponse(
			ticket.getId(),
			ticket.getTitle(),
			ticket.getStatus(),
			ticket.getPriority(),
			ticket.getAuthor().getId(),
			ticket.getAssignee() != null ? ticket.getAssignee().getId() : null,
			ticket.getCategory().getId(),
			ticket.getCategory().getName(),
			ticket.getCreatedAt(),
			ticket.getUpdatedAt(),
			ticket.getDueAt()
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
