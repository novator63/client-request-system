package com.example.app.ticket.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class TicketBadRequestException extends BusinessException {

	public TicketBadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}