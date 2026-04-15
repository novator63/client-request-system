package com.example.app.ticket.exception;

import com.example.app.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class TicketNotFoundException extends BusinessException {

	public TicketNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Ticket not found");
	}
}
