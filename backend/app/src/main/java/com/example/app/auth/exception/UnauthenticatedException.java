package com.example.app.auth.exception;

public class UnauthenticatedException extends RuntimeException {

	public UnauthenticatedException() {
		super("Authentication required");
	}
}
