package com.example.app.ticket.dto.request;

import com.example.app.ticket.entity.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketClassificationRequest(
	@NotNull(message = "Category is required")
	Long categoryId,

	@NotNull(message = "Ticket priority is required")
	TicketPriority priority
) {
}