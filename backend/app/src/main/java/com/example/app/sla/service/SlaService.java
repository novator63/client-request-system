package com.example.app.sla.service;

import com.example.app.ticket.dto.response.TicketListItemResponse;
import com.example.app.ticket.entity.TicketPriority;
import com.example.app.ticket.entity.TicketStatus;
import com.example.app.ticket.mapper.TicketMapper;
import com.example.app.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SlaService {

	private final TicketRepository ticketRepository;
	private final TicketMapper ticketMapper;

	public SlaService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
		this.ticketRepository = ticketRepository;
		this.ticketMapper = ticketMapper;
	}

	public LocalDateTime calculateDueAt(TicketPriority priority) {
		LocalDateTime now = LocalDateTime.now();
		return switch (priority) {
			case HIGH -> now.plusHours(4);
			case MEDIUM -> now.plusDays(1);
			case LOW -> now.plusDays(3);
		};
	}

	public List<TicketListItemResponse> getOverdueTickets() {
		return ticketRepository.findAllByDueAtBeforeAndStatusNotOrderByDueAtAsc(LocalDateTime.now(), TicketStatus.CLOSED)
			.stream()
			.map(ticketMapper::toListItemResponse)
			.toList();
	}
}
