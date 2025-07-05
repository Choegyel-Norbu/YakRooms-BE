package com.yakrooms.be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceNotFoundException extends RuntimeException {

	// Constructor with default message
	public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
		super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
	}

	// Constructor with custom message
	public ResourceNotFoundException(String message) {
		super(message);
	}

	// Constructor with cause
	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
