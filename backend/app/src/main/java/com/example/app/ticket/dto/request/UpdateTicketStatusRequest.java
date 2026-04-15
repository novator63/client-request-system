package com.example.app.ticket.dto.request;

import com.example.app.ticket.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
	@NotNull(message = "Ticket status is required")
	TicketStatus status
) {
}