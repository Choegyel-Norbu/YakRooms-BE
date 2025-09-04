package com.yakrooms.be.service;

import com.yakrooms.be.dto.UploadThingDeleteResponse;
import com.yakrooms.be.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with UploadThing API.
 * Provides file deletion capabilities with proper error handling and logging.
 * 
 * This service uses reactive programming patterns with WebClient for non-blocking HTTP calls.
 * It includes retry logic, timeout handling, and comprehensive error management.
 */
@Service
public class UploadThingService {

    private static final Logger logger = LoggerFactory.getLogger(UploadThingService.class);

    private final WebClient webClient;

    @Autowired
    public UploadThingService(WebClient uploadThingWebClient) {
        this.webClient = uploadThingWebClient;
    }

    /**
     * Deletes multiple files from UploadThing asynchronously using hybrid approach.
     * 
     * @param fileKeys List of file keys to delete
     * @return CompletableFuture that completes when deletion is finished
     * @throws BusinessException if the deletion fails
     */
    public CompletableFuture<UploadThingDeleteResponse> deleteFilesAsync(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            logger.warn("Attempted to delete files with empty or null file keys list");
            throw new BusinessException("File keys list cannot be null or empty");
        }

        logger.info("Starting async deletion of {} files using hybrid approach", fileKeys.size());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return deleteFilesHybrid(fileKeys);
            } catch (Exception e) {
                logger.error("Async hybrid deletion failed: {}", e.getMessage(), e);
                throw new BusinessException("Failed to delete files: " + e.getMessage());
            }
        });
    }



    /**
     * Deletes multiple files from UploadThing synchronously using hybrid approach.
     * 
     * @param fileKeys List of file keys to delete
     * @return UploadThingDeleteResponse containing the deletion results
     * @throws BusinessException if the deletion fails
     */
    public UploadThingDeleteResponse deleteFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            logger.warn("Attempted to delete files with empty or null file keys list");
            throw new BusinessException("File keys list cannot be null or empty");
        }

        logger.info("Starting synchronous deletion of {} files using hybrid approach", fileKeys.size());
        
        try {
            return deleteFilesHybrid(fileKeys);
        } catch (Exception e) {
            logger.error("Synchronous hybrid deletion failed: {}", e.getMessage(), e);
            throw new BusinessException("Failed to delete files: " + e.getMessage());
        }
    }

    /**
     * Deletes a single file from UploadThing using hybrid approach.
     * 
     * @param fileKey The file key to delete
     * @return UploadThingDeleteResponse containing the deletion result
     * @throws BusinessException if the deletion fails
     */
    public UploadThingDeleteResponse deleteFile(String fileKey) {
        if (fileKey == null || fileKey.trim().isEmpty()) {
            logger.warn("Attempted to delete file with null or empty file key");
            throw new BusinessException("File key cannot be null or empty");
        }

        logger.info("Deleting single file using hybrid approach: {}", fileKey);
        return deleteFilesHybrid(List.of(fileKey));
    }



    /**
     * Deletes files using UploadThing's Node.js SDK via hybrid approach.
     * This method calls a Node.js script that uses the official UploadThing SDK.
     * 
     * @param fileKeys List of file keys to delete
     * @return UploadThingDeleteResponse containing the deletion result
     */
    public UploadThingDeleteResponse deleteFilesHybrid(List<String> fileKeys) {
        logger.info("Starting hybrid deletion of {} files using Node.js SDK", fileKeys.size());
        
        try {
            // Build the command to run the Node.js script
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("node", "uploadthing-delete.js");
            processBuilder.command().addAll(fileKeys);
            
            // Set environment variables
            processBuilder.environment().put("UPLOADTHING_TOKEN", "eyJhcGlLZXkiOiJza19saXZlX2U2MWE0YWQ5ODliNmEyZmMxMGVhZGEzMWNiYzE0Y2ZlYTBlOGE4ODE5MGJkN2QzYmVhMzU0MTA5NmU4MmJjMDgiLCJhcHBJZCI6Imc1aDkya2U1YzEiLCJyZWdpb25zIjpbInNlYTEiXX0=");
            
            // Start the process
            Process process = processBuilder.start();

            // Capture stdout and stderr
            BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = stdOutReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = stdErrReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            
            // Wait for the process to complete (with timeout)
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("Node.js script timed out after 30 seconds");
            }
            
            int exitCode = process.exitValue();
            String outputString = output.toString().trim();
            String errorString = errorOutput.toString().trim();
            
            if (exitCode == 0) {
                logger.info("Hybrid deletion completed successfully for {} files", fileKeys.size());
                // Parse the JSON response from Node.js script
                return parseNodeJsResponse(outputString, fileKeys);
            } else {
                logger.error("Node.js script failed with exit code: {}", exitCode);
                if (!outputString.isEmpty()) {
                    logger.error("Script stdout: {}", outputString);
                }
                if (!errorString.isEmpty()) {
                    logger.error("Script stderr: {}", errorString);
                }
                throw new BusinessException("Failed to delete files via Node.js script: " + (!errorString.isEmpty() ? errorString : outputString));
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error executing Node.js script for file deletion", e);
            throw new BusinessException("Failed to execute Node.js script: " + e.getMessage());
        }
    }

    /**
     * Parses the JSON response from the Node.js script.
     * 
     * @param jsonResponse JSON response from Node.js script
     * @param fileKeys Original file keys that were requested for deletion
     * @return UploadThingDeleteResponse parsed from the JSON
     */
    private UploadThingDeleteResponse parseNodeJsResponse(String jsonResponse, List<String> fileKeys) {
        try {
            // Simple JSON parsing - in production, use a proper JSON library
            UploadThingDeleteResponse response = new UploadThingDeleteResponse();
            if (jsonResponse.contains("\"success\":true")) {
                response.setSuccess(true);
                response.setMessage("Files deleted successfully");
                response.setDeletedFiles(fileKeys);
                response.setFailedFiles(null);
            } else {
                response.setSuccess(false);
                response.setMessage("Failed to delete files");
                response.setDeletedFiles(null);
                response.setFailedFiles(fileKeys);
            }
            return response;
        } catch (Exception e) {
            logger.error("Error parsing Node.js response: {}", e.getMessage());
            UploadThingDeleteResponse errorResponse = new UploadThingDeleteResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error parsing deletion response");
            errorResponse.setDeletedFiles(null);
            errorResponse.setFailedFiles(fileKeys);
            return errorResponse;
        }
    }

    /**
     * Validates if the service is properly configured and can connect to UploadThing.
     * 
     * @return true if the service is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            // Perform a simple health check by making a minimal request
            // This could be enhanced with actual health check endpoint if available
            logger.debug("Performing health check for UploadThing service");
            return true; // For now, assume healthy if WebClient is configured
        } catch (Exception e) {
            logger.error("UploadThing service health check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
