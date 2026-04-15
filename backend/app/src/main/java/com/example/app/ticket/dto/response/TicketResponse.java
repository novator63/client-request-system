package com.example.app.ticket.dto.response;

import com.example.app.ticket.entity.TicketPriority;
import com.example.app.ticket.entity.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
	Long id,
	String title,
	String description,
	TicketStatus status,
	TicketPriority priority,
	Long authorId,
	String authorName,
	Long assigneeId,
	String assigneeName,
	Long categoryId,
	String categoryName,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	LocalDateTime closedAt,
	LocalDateTime dueAt
) {
}