package com.example.app.ticket.exception;

public class TicketNotFoundException extends RuntimeException {

	public TicketNotFoundException() {
		super("Ticket not found");
	}
}
