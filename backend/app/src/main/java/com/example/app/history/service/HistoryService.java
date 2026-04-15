package com.example.app.history.service;

import com.example.app.history.dto.response.HistoryEntryResponse;
import com.example.app.history.dto.response.TicketHistoryResponse;
import com.example.app.history.entity.HistoryActionType;
import com.example.app.history.entity.HistoryEntry;
import com.example.app.history.mapper.HistoryMapper;
import com.example.app.history.repository.HistoryRepository;
import com.example.app.ticket.entity.Ticket;
import com.example.app.ticket.entity.TicketStatus;
import com.example.app.ticket.exception.TicketNotFoundException;
import com.example.app.ticket.repository.TicketRepository;
import com.example.app.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HistoryService {

	private final HistoryRepository historyRepository;
	private final TicketRepository ticketRepository;
	private final HistoryMapper historyMapper;

	public HistoryService(
		HistoryRepository historyRepository,
		TicketRepository ticketRepository,
		HistoryMapper historyMapper
	) {
		this.historyRepository = historyRepository;
		this.ticketRepository = ticketRepository;
		this.historyMapper = historyMapper;
	}

	public TicketHistoryResponse getByTicketId(Long ticketId) {
		if (!ticketRepository.existsById(ticketId)) {
			throw new TicketNotFoundException();
		}

		List<HistoryEntryResponse> entries = historyRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticketId)
			.stream()
			.map(historyMapper::toResponse)
			.toList();

		return new TicketHistoryResponse(ticketId, entries);
	}

	@Transactional
	public void recordTicketCreated(Ticket ticket, User actor) {
		save(ticket, actor, HistoryActionType.TICKET_CREATED, "Заявка создана");
	}

	@Transactional
	public void recordAssigned(Ticket ticket, User actor, User assignee) {
		save(ticket, actor, HistoryActionType.ASSIGNED, "Назначен исполнитель: " + assignee.getFullName());
	}

	@Transactional
	public void recordCategoryChanged(Ticket ticket, User actor, String oldCategoryName, String newCategoryName) {
		save(
			ticket,
			actor,
			HistoryActionType.CATEGORY_CHANGED,
			"Категория изменена с " + oldCategoryName + " на " + newCategoryName
		);
	}

	@Transactional
	public void recordStatusChanged(Ticket ticket, User actor, TicketStatus oldStatus, TicketStatus newStatus) {
		save(
			ticket,
			actor,
			HistoryActionType.STATUS_CHANGED,
			"Статус изменен с " + oldStatus + " на " + newStatus
		);
	}

	@Transactional
	public void recordClosed(Ticket ticket, User actor) {
		save(ticket, actor, HistoryActionType.CLOSED, "Заявка закрыта");
	}

	@Transactional
	public void recordCommentAdded(Ticket ticket, User actor) {
		save(ticket, actor, HistoryActionType.COMMENT_ADDED, "Добавлен комментарий");
	}

	private void save(Ticket ticket, User actor, HistoryActionType actionType, String description) {
		HistoryEntry entry = new HistoryEntry();
		entry.setTicket(ticket);
		entry.setActor(actor);
		entry.setActionType(actionType);
		entry.setDescription(description);
		historyRepository.save(entry);
	}
}
