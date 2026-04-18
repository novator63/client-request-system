package com.example.app.ticket.repository;

import com.example.app.ticket.entity.Ticket;
import com.example.app.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
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

	interface AssigneeCountProjection {
		Long getAssigneeId();

		Long getCount();
	}

	List<Ticket> findAllByOrderByCreatedAtDesc();

	List<Ticket> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);

	List<Ticket> findAllByAssigneeIdOrderByCreatedAtDesc(Long assigneeId);

	boolean existsByIdAndAuthorId(Long id, Long authorId);

	boolean existsByIdAndAssigneeId(Long id, Long assigneeId);

	List<Ticket> findAllByDueAtBeforeAndStatusNotOrderByDueAtAsc(LocalDateTime dueAt, TicketStatus status);

	long countByStatus(TicketStatus status);

	long countByDueAtBeforeAndStatusNot(LocalDateTime dueAt, TicketStatus status);

	@Query("select t.status as status, count(t) as count from Ticket t group by t.status")
	List<StatusCountProjection> countTicketsByStatus();

	@Query("select t.category.name as name, count(t) as count from Ticket t group by t.category.id, t.category.name order by t.category.name")
	List<CategoryCountProjection> countTicketsByCategory();

	@Query("""
		select t.assignee.id as assigneeId, count(t) as count
		from Ticket t
		where t.assignee.id in :assigneeIds and t.status <> :closedStatus
		group by t.assignee.id
	""")
	List<AssigneeCountProjection> countActiveTicketsByAssigneeIds(
		@Param("assigneeIds") List<Long> assigneeIds,
		@Param("closedStatus") TicketStatus closedStatus
	);

	@Query("""
		select t.assignee.id as assigneeId, count(t) as count
		from Ticket t
		where t.assignee.id in :assigneeIds and t.status <> :closedStatus and t.dueAt is not null and t.dueAt < :now
		group by t.assignee.id
	""")
	List<AssigneeCountProjection> countOverdueOpenTicketsByAssigneeIds(
		@Param("assigneeIds") List<Long> assigneeIds,
		@Param("closedStatus") TicketStatus closedStatus,
		@Param("now") LocalDateTime now
	);
}
