package com.yakrooms.be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Automatically returns 409 status
public class ResourceConflictException extends RuntimeException {

	// Constructor with default message
	public ResourceConflictException(String resourceName, String fieldName, Object fieldValue) {
		super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
	}

	// Constructor with custom message
	public ResourceConflictException(String message) {
		super(message);
	}

	// Constructor with cause
	public ResourceConflictException(String message, Throwable cause) {
		super(message, cause);
	}
}
