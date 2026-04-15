package com.example.app.ticket.dto.response;

import com.example.app.ticket.entity.TicketPriority;
import com.example.app.ticket.entity.TicketStatus;

import java.time.LocalDateTime;

public record TicketListItemResponse(
	Long id,
	String title,
	TicketStatus status,
	TicketPriority priority,
	Long authorId,
	Long assigneeId,
	Long categoryId,
	String categoryName,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	LocalDateTime dueAt
) {
}
