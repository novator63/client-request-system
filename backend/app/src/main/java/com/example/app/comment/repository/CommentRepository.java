package com.example.app.comment.repository;

import com.example.app.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
