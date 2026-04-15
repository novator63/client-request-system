package com.example.app.history.dto.response;

import java.util.List;

public record TicketHistoryResponse(
	Long ticketId,
	List<HistoryEntryResponse> entries
) {
}
