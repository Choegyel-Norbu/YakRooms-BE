package com.yakrooms.be.dto.response;

import java.time.Instant;

public class ErrorResponse {
	private int status;
	private String error;
	private String message;
	private Instant timestamp;

	public ErrorResponse(int status, String error, String message, Instant timestamp) {
		super();
		this.status = status;
		this.error = error;
		this.message = message;
		this.timestamp = timestamp;
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

}
