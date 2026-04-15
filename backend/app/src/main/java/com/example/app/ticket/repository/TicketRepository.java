package com.example.app.ticket.repository;

import com.example.app.ticket.entity.Ticket;
import com.example.app.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findAllByOrderByCreatedAtDesc();

	List<Ticket> findAllByDueAtBeforeAndStatusNotOrderByDueAtAsc(LocalDateTime dueAt, TicketStatus status);
}
