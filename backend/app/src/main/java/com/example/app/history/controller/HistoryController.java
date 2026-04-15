package com.example.app.history.controller;

import com.example.app.history.dto.response.TicketHistoryResponse;
import com.example.app.history.service.HistoryService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history/tickets")
@Validated
public class HistoryController {

	private final HistoryService historyService;

	public HistoryController(HistoryService historyService) {
		this.historyService = historyService;
	}

	@GetMapping("/{ticketId}")
	public TicketHistoryResponse getByTicketId(
		@PathVariable @Positive(message = "Ticket id must be positive") Long ticketId
	) {
		return historyService.getByTicketId(ticketId);
	}
}
