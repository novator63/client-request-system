package com.example.app.ticket.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
	@NotNull(message = "Assignee is required")
	Long assigneeId
) {
}
