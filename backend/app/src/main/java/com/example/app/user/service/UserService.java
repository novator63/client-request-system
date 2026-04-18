package com.example.app.user.service;

import com.example.app.auth.security.CurrentUserService;
import com.example.app.ticket.entity.TicketStatus;
import com.example.app.ticket.repository.TicketRepository;
import com.example.app.user.dto.response.OperatorOptionResponse;
import com.example.app.user.dto.response.UserResponse;
import com.example.app.user.entity.User;
import com.example.app.user.entity.UserRole;
import com.example.app.user.exception.UserNotFoundException;
import com.example.app.user.mapper.UserMapper;
import com.example.app.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final CurrentUserService currentUserService;
	private final TicketRepository ticketRepository;

	public UserService(
		UserRepository userRepository,
		UserMapper userMapper,
		CurrentUserService currentUserService,
		TicketRepository ticketRepository
	) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.currentUserService = currentUserService;
		this.ticketRepository = ticketRepository;
	}

	public UserResponse findByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(UserNotFoundException::new);
		return userMapper.toResponse(user);
	}

	public UserResponse getById(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(UserNotFoundException::new);
		return userMapper.toResponse(user);
	}

	public UserResponse getCurrentUser() {
		return userMapper.toResponse(currentUserService.requireCurrentUser());
	}

	public List<OperatorOptionResponse> getAvailableOperators() {
		List<User> operators = userRepository.findAllByRoleAndActiveTrueOrderByFullNameAsc(UserRole.OPERATOR);
		if (operators.isEmpty()) {
			return List.of();
		}

		List<Long> operatorIds = operators.stream().map(User::getId).toList();
		Map<Long, Long> activeCounts = toCountMap(
			ticketRepository.countActiveTicketsByAssigneeIds(operatorIds, TicketStatus.CLOSED)
		);
		Map<Long, Long> overdueCounts = toCountMap(
			ticketRepository.countOverdueOpenTicketsByAssigneeIds(operatorIds, TicketStatus.CLOSED, LocalDateTime.now())
		);

		return operators
			.stream()
			.map(operator -> userMapper.toOperatorOptionResponse(
				operator,
				activeCounts.getOrDefault(operator.getId(), 0L),
				overdueCounts.getOrDefault(operator.getId(), 0L)
			))
			.toList();
	}

	private Map<Long, Long> toCountMap(List<TicketRepository.AssigneeCountProjection> projections) {
		if (projections == null || projections.isEmpty()) {
			return Collections.emptyMap();
		}

		return projections.stream().collect(Collectors.toMap(
			TicketRepository.AssigneeCountProjection::getAssigneeId,
			TicketRepository.AssigneeCountProjection::getCount,
			(existing, replacement) -> existing
		));
	}
}
