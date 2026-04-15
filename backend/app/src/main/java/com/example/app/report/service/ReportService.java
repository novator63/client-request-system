package com.example.app.report.service;

import com.example.app.report.dto.response.CategoryReportItemResponse;
import com.example.app.report.dto.response.StatusReportItemResponse;
import com.example.app.report.dto.response.SummaryReportResponse;
import com.example.app.ticket.entity.TicketStatus;
import com.example.app.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

	private final TicketRepository ticketRepository;

	public ReportService(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	public SummaryReportResponse getSummary() {
		return new SummaryReportResponse(
			ticketRepository.count(),
			ticketRepository.countByStatus(TicketStatus.NEW),
			ticketRepository.countByStatus(TicketStatus.IN_PROGRESS),
			ticketRepository.countByStatus(TicketStatus.CLOSED),
			ticketRepository.countByDueAtBeforeAndStatusNot(LocalDateTime.now(), TicketStatus.CLOSED)
		);
	}

	public List<StatusReportItemResponse> getByStatus() {
		return ticketRepository.countTicketsByStatus()
			.stream()
			.map(item -> new StatusReportItemResponse(item.getStatus().name(), item.getCount()))
			.toList();
	}

	public List<CategoryReportItemResponse> getByCategory() {
		return ticketRepository.countTicketsByCategory()
			.stream()
			.map(item -> new CategoryReportItemResponse(item.getName(), item.getCount()))
			.toList();
	}
}
