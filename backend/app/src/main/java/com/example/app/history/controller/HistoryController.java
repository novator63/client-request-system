package com.example.app.history.controller;

import com.example.app.history.dto.response.TicketHistoryResponse;
import com.example.app.history.service.HistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history/tickets")
public class HistoryController {

	private final HistoryService historyService;

	public HistoryController(HistoryService historyService) {
		this.historyService = historyService;
	}

	@GetMapping("/{ticketId}")
	public TicketHistoryResponse getByTicketId(@PathVariable Long ticketId) {
		return historyService.getByTicketId(ticketId);
	}
}
