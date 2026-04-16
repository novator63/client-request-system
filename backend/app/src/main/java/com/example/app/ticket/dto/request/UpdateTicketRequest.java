package com.example.app.ticket.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
	@Size(max = 255, message = "Ticket title must be at most 255 characters")
	String title,

	@Size(max = 500, message = "Ticket description must be at most 500 characters")
	String description
) {
}
