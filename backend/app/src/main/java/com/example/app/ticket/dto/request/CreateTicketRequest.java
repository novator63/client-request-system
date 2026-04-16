package com.example.app.ticket.dto.request;

import com.example.app.ticket.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
	@NotBlank(message = "Ticket title is required")
	@Size(max = 255, message = "Ticket title must be at most 255 characters")
	String title,

	@NotBlank(message = "Ticket description is required")
	@Size(max = 500, message = "Ticket description must be at most 500 characters")
	String description,

	@NotNull(message = "Ticket priority is required")
	TicketPriority priority,

	@NotNull(message = "Category is required")
	@Positive(message = "Category id must be positive")
	Long categoryId
) {
}
