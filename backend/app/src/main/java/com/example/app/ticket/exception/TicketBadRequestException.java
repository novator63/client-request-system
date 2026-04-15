package com.example.app.ticket.exception;

public class TicketBadRequestException extends RuntimeException {

	public TicketBadRequestException(String message) {
		super(message);
	}
}