package com.yakrooms.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for UploadThing delete files response.
 * Represents the response from UploadThing API after file deletion.
 */
public class UploadThingDeleteResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("deletedFiles")
    private List<String> deletedFiles;

    @JsonProperty("failedFiles")
    private List<String> failedFiles;

    public UploadThingDeleteResponse() {
        // Default constructor for JSON deserialization
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDeletedFiles() {
        return deletedFiles;
    }

    public void setDeletedFiles(List<String> deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    public List<String> getFailedFiles() {
        return failedFiles;
    }

    public void setFailedFiles(List<String> failedFiles) {
        this.failedFiles = failedFiles;
    }

    @Override
    public String toString() {
        return "UploadThingDeleteResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", deletedFiles=" + deletedFiles +
                ", failedFiles=" + failedFiles +
                '}';
    }
}
