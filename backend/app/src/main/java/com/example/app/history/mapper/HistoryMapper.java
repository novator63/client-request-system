package com.example.app.history.mapper;

import com.example.app.history.dto.response.HistoryEntryResponse;
import com.example.app.history.entity.HistoryEntry;
import org.springframework.stereotype.Component;

@Component
public class HistoryMapper {

	public HistoryEntryResponse toResponse(HistoryEntry entry) {
		return new HistoryEntryResponse(
			entry.getId(),
			entry.getActionType(),
			entry.getDescription(),
			entry.getActor().getId(),
			entry.getActor().getFullName(),
			entry.getCreatedAt()
		);
	}
}
