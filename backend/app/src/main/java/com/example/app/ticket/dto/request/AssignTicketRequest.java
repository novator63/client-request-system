package com.example.app.ticket.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTicketRequest(
	@NotNull(message = "Assignee is required")
	@Positive(message = "Assignee id must be positive")
	Long assigneeId
) {
}
