package com.example.app.history.repository;

import com.example.app.history.entity.HistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntry, Long> {
	List<HistoryEntry> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
