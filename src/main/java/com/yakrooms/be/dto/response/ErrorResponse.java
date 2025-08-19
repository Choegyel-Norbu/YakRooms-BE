package com.yakrooms.be.dto.response;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {
	private int status;
	private String error;
	private String message;
	private String userMessage; // User-friendly message for display
	private String field; // Field that caused the error (optional)
	private String errorCode; // Error code for programmatic handling
	private Instant timestamp;
	private Map<String, Object> details; // Additional error details

	// Original constructor for backward compatibility
	public ErrorResponse(int status, String error, String message, Instant timestamp) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.timestamp = timestamp;
	}

	// Enhanced constructor with user-friendly message
	public ErrorResponse(int status, String error, String message, String userMessage, Instant timestamp) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.userMessage = userMessage;
		this.timestamp = timestamp;
	}

	// Full constructor with all fields
	public ErrorResponse(int status, String error, String message, String userMessage, 
			String field, String errorCode, Instant timestamp, Map<String, Object> details) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.userMessage = userMessage;
		this.field = field;
		this.errorCode = errorCode;
		this.timestamp = timestamp;
		this.details = details;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

}
