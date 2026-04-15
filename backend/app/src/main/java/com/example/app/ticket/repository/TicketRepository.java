package com.example.app.ticket.repository;

import com.example.app.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findAllByOrderByCreatedAtDesc();
}
