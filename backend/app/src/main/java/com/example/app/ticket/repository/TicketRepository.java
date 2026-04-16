package com.example.app.ticket.repository;

import com.example.app.ticket.entity.Ticket;
import com.example.app.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	interface StatusCountProjection {
		TicketStatus getStatus();

		Long getCount();
	}

	interface CategoryCountProjection {
		String getName();

		Long getCount();
	}

	List<Ticket> findAllByOrderByCreatedAtDesc();

	List<Ticket> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

	boolean existsByIdAndAuthorId(Long id, Long authorId);

	List<Ticket> findAllByDueAtBeforeAndStatusNotOrderByDueAtAsc(LocalDateTime dueAt, TicketStatus status);

	long countByStatus(TicketStatus status);

	long countByDueAtBeforeAndStatusNot(LocalDateTime dueAt, TicketStatus status);

	@Query("select t.status as status, count(t) as count from Ticket t group by t.status")
	List<StatusCountProjection> countTicketsByStatus();

	@Query("select t.category.name as name, count(t) as count from Ticket t group by t.category.id, t.category.name order by t.category.name")
	List<CategoryCountProjection> countTicketsByCategory();
}
