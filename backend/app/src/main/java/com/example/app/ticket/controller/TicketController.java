package com.example.app.ticket.controller;

import com.example.app.ticket.dto.request.AssignTicketRequest;
import com.example.app.ticket.dto.request.CreateTicketRequest;
import com.example.app.ticket.dto.request.UpdateTicketClassificationRequest;
import com.example.app.ticket.dto.request.UpdateTicketRequest;
import com.example.app.ticket.dto.request.UpdateTicketStatusRequest;
import com.example.app.ticket.dto.response.TicketListItemResponse;
import com.example.app.ticket.dto.response.TicketResponse;
import com.example.app.ticket.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/tickets")
@Validated
public class TicketController {

	private final TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@PostMapping
	@ResponseStatus(CREATED)
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CLIENT')")
	public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
		return ticketService.create(request);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR','CLIENT')")
	public List<TicketListItemResponse> getAll() {
		return ticketService.getAll();
	}

	@GetMapping("/{id}")
	@PreAuthorize("@ticketAccess.canReadTicket(#id)")
	public TicketResponse getById(@PathVariable @Positive(message = "Ticket id must be positive") Long id) {
		return ticketService.getById(id);
	}

	@PatchMapping("/{id}")
	@PreAuthorize("@ticketAccess.canModifyTicket(#id)")
	public TicketResponse update(
		@PathVariable @Positive(message = "Ticket id must be positive") Long id,
		@Valid @RequestBody UpdateTicketRequest request
	) {
		return ticketService.update(id, request);
	}

	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasRole('ADMIN')")
	public TicketResponse assign(
		@PathVariable @Positive(message = "Ticket id must be positive") Long id,
		@Valid @RequestBody AssignTicketRequest request
	) {
		return ticketService.assign(id, request);
	}

	@PatchMapping("/{id}/classification")
	@PreAuthorize("hasRole('ADMIN')")
	public TicketResponse updateClassification(
		@PathVariable @Positive(message = "Ticket id must be positive") Long id,
		@Valid @RequestBody UpdateTicketClassificationRequest request
	) {
		return ticketService.updateClassification(id, request);
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("@ticketAccess.canModifyTicket(#id)")
	public TicketResponse updateStatus(
		@PathVariable @Positive(message = "Ticket id must be positive") Long id,
		@Valid @RequestBody UpdateTicketStatusRequest request
	) {
		return ticketService.updateStatus(id, request);
	}

	@PostMapping("/{id}/close")
	@PreAuthorize("@ticketAccess.canModifyTicket(#id)")
	public TicketResponse close(@PathVariable @Positive(message = "Ticket id must be positive") Long id) {
		return ticketService.close(id);
	}
}
