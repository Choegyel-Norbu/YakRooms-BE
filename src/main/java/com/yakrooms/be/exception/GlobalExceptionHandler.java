package com.yakrooms.be.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yakrooms.be.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflict(ResourceConflictException ex) {
        logger.warn("Resource conflict occurred: {}", ex.getMessage());
        
        String userMessage;
        String field = null;
        String errorCode = "RESOURCE_CONFLICT";
        Map<String, Object> details = new HashMap<>();
        
        // Provide user-friendly messages based on the conflict type
        if (ex.getMessage().contains("Staff already exists with email")) {
            userMessage = "This email address is already registered. Please use a different email or check if the staff member already exists.";
            field = "email";
            errorCode = "EMAIL_ALREADY_EXISTS";
            details.put("suggestion", "Try using a different email address");
            details.put("action", "check_existing_staff");
        } else if (ex.getMessage().contains("Hotel already exists")) {
            userMessage = "A hotel with these details already exists. Please check the information and try again.";
            errorCode = "HOTEL_ALREADY_EXISTS";
        } else {
            userMessage = "The resource you're trying to create already exists. Please check your information and try again.";
        }
        
        details.put("timestamp", Instant.now().toString());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            userMessage,
            field,
            errorCode,
            Instant.now(),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        
        String userMessage;
        String errorCode = "RESOURCE_NOT_FOUND";
        Map<String, Object> details = new HashMap<>();
        
        // Provide user-friendly messages based on the resource type
        if (ex.getMessage().contains("Hotel not found")) {
            userMessage = "The selected hotel was not found. Please select a valid hotel from the list.";
            errorCode = "HOTEL_NOT_FOUND";
            details.put("suggestion", "Refresh the hotel list and try again");
        } else if (ex.getMessage().contains("Staff not found")) {
            userMessage = "The staff member you're looking for was not found. They may have been removed or transferred.";
            errorCode = "STAFF_NOT_FOUND";
        } else if (ex.getMessage().contains("User not found")) {
            userMessage = "User account not found. Please check the details and try again.";
            errorCode = "USER_NOT_FOUND";
        } else {
            userMessage = "The requested resource was not found. Please check your request and try again.";
        }
        
        details.put("timestamp", Instant.now().toString());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            userMessage,
            null,
            errorCode,
            Instant.now(),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Business Rule Violation",
            ex.getMessage(),
            Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        String technicalMessage = "Validation failed: " + String.join(", ", fieldErrors.values());
        String userMessage = "Please check the following fields and try again: " + 
                            String.join(", ", fieldErrors.keySet());
        
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);
        details.put("errorCount", fieldErrors.size());
        details.put("timestamp", Instant.now().toString());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            technicalMessage,
            userMessage,
            fieldErrors.size() == 1 ? fieldErrors.keySet().iterator().next() : null, // Single field if only one error
            "VALIDATION_FAILED",
            Instant.now(),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Argument",
            ex.getMessage(),
            Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        logger.error("Illegal state error occurred: {}", ex.getMessage(), ex);
        
        // Check if it's an email-related error
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email")) {
            ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Email Service Unavailable",
                "Unable to send email notification. The operation was completed but notification failed.",
                Instant.now()
            );
            return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "System Error",
            "An unexpected error occurred. Please try again.",
            Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
