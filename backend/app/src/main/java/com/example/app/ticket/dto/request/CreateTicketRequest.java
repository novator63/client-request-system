package com.example.app.ticket.dto.request;

import com.example.app.ticket.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
	@NotBlank(message = "Ticket title is required")
	@Size(max = 255, message = "Ticket title must be at most 255 characters")
	String title,

	@NotBlank(message = "Ticket description is required")
	@Size(max = 5000, message = "Ticket description must be at most 5000 characters")
	String description,

	@NotNull(message = "Ticket priority is required")
	TicketPriority priority,

	@NotNull(message = "Category is required")
	Long categoryId
) {
}
