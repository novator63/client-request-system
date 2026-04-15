package com.example.app.ticket.dto.request;

import com.example.app.ticket.entity.TicketPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTicketClassificationRequest(
	@NotNull(message = "Category is required")
	@Positive(message = "Category id must be positive")
	Long categoryId,

	@NotNull(message = "Ticket priority is required")
	TicketPriority priority
) {
}