package com.yakrooms.be.controller;

import com.yakrooms.be.dto.UploadThingDeleteResponse;
import com.yakrooms.be.service.UploadThingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for UploadThing file operations.
 * Provides endpoints for file deletion with proper validation and error handling.
 * 
 * This controller follows REST best practices and includes comprehensive input validation.
 */
@RestController
@RequestMapping("/api/v1/uploadthing")
public class UploadThingController {

    private static final Logger logger = LoggerFactory.getLogger(UploadThingController.class);

    private final UploadThingService uploadThingService;

    @Autowired
    public UploadThingController(UploadThingService uploadThingService) {
        this.uploadThingService = uploadThingService;
    }

    /**
     * Deletes multiple files from UploadThing using hybrid approach.
     * 
     * @param fileKeys List of file keys to delete
     * @return ResponseEntity containing the deletion results
     */
    @DeleteMapping("/files")
    public ResponseEntity<UploadThingDeleteResponse> deleteFiles(
            @RequestBody @Valid @NotEmpty(message = "File keys list cannot be empty") List<String> fileKeys) {
        
        logger.info("Received request to delete {} files (redirecting to hybrid approach)", fileKeys.size());
        
        try {
            // Redirect to hybrid approach since direct API is not working
            UploadThingDeleteResponse response = uploadThingService.deleteFilesHybrid(fileKeys);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete files: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to delete files: " + e.getMessage()));
        }
    }

    /**
     * Deletes a single file from UploadThing using hybrid approach.
     * 
     * @param fileKey The file key to delete
     * @return ResponseEntity containing the deletion result
     */
    @DeleteMapping("/files/{fileKey}")
    public ResponseEntity<UploadThingDeleteResponse> deleteFile(
            @PathVariable @Valid @NotEmpty(message = "File key cannot be empty") String fileKey) {
        
        logger.info("Received request to delete file: {} (redirecting to hybrid approach)", fileKey);
        
        try {
            // Redirect to hybrid approach since direct API is not working
            UploadThingDeleteResponse response = uploadThingService.deleteFilesHybrid(List.of(fileKey));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", fileKey, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to delete file: " + e.getMessage()));
        }
    }

    /**
     * Deletes multiple files from UploadThing asynchronously using hybrid approach.
     * 
     * @param fileKeys List of file keys to delete
     * @return ResponseEntity containing a CompletableFuture with the deletion results
     */
    @DeleteMapping("/files/async")
    public ResponseEntity<CompletableFuture<UploadThingDeleteResponse>> deleteFilesAsync(
            @RequestBody @Valid @NotEmpty(message = "File keys list cannot be empty") List<String> fileKeys) {
        
        logger.info("Received async request to delete {} files using hybrid approach", fileKeys.size());
        
        try {
            // Use hybrid approach for async deletion as well
            CompletableFuture<UploadThingDeleteResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadThingService.deleteFilesHybrid(fileKeys);
                } catch (Exception e) {
                    logger.error("Async hybrid deletion failed", e);
                    return createErrorResponse("Failed to delete files: " + e.getMessage());
                }
            });
            return ResponseEntity.accepted().body(future);
        } catch (Exception e) {
            logger.error("Failed to initiate async file deletion: {}", e.getMessage(), e);
            CompletableFuture<UploadThingDeleteResponse> errorFuture = CompletableFuture.completedFuture(
                    createErrorResponse("Failed to initiate file deletion: " + e.getMessage()));
            return ResponseEntity.internalServerError().body(errorFuture);
        }
    }



    /**
     * Deletes files using the hybrid Node.js approach.
     * 
     * @param fileKeys List of file keys to delete
     * @return ResponseEntity with deletion results
     */
    @DeleteMapping("/files/hybrid")
    public ResponseEntity<UploadThingDeleteResponse> deleteFilesHybrid(
            @RequestBody @Valid @NotEmpty(message = "File keys list cannot be empty") List<String> fileKeys) {
        
        logger.info("Received hybrid deletion request for {} files", fileKeys.size());
        
        try {
            UploadThingDeleteResponse response = uploadThingService.deleteFilesHybrid(fileKeys);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Hybrid file deletion failed", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to delete files via hybrid approach: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint for UploadThing service.
     * 
     * @return ResponseEntity indicating service health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Received health check request for UploadThing service");
        
        boolean isHealthy = uploadThingService.isHealthy();
        if (isHealthy) {
            return ResponseEntity.ok("UploadThing service is healthy");
        } else {
            return ResponseEntity.status(503).body("UploadThing service is unhealthy");
        }
    }

    /**
     * Creates an error response with the given message.
     * 
     * @param message Error message
     * @return UploadThingDeleteResponse with error details
     */
    private UploadThingDeleteResponse createErrorResponse(String message) {
        UploadThingDeleteResponse response = new UploadThingDeleteResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
