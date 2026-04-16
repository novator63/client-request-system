package com.example.app.sla.controller;

import com.example.app.sla.service.SlaService;
import com.example.app.ticket.dto.response.TicketListItemResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sla")
public class SlaController {

	private final SlaService slaService;

	public SlaController(SlaService slaService) {
		this.slaService = slaService;
	}

	@GetMapping("/overdue")
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
	public List<TicketListItemResponse> getOverdue() {
		return slaService.getOverdueTickets();
	}
}
