package com.example.app.history.dto.response;

import com.example.app.history.entity.HistoryActionType;

import java.time.LocalDateTime;

public record HistoryEntryResponse(
	Long id,
	HistoryActionType actionType,
	String description,
	Long actorId,
	String actorFullName,
	LocalDateTime createdAt
) {
}
