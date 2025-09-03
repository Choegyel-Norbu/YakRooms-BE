package com.yakrooms.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for UploadThing delete files request.
 * Represents the payload sent to UploadThing API for file deletion.
 */
public class UploadThingDeleteRequest {

    @JsonProperty("fileKeys")
    @NotEmpty(message = "File keys list cannot be empty")
    @Size(max = 100, message = "Cannot delete more than 100 files at once")
    private List<String> fileKeys;

    public UploadThingDeleteRequest() {
        // Default constructor for JSON deserialization
    }

    public UploadThingDeleteRequest(List<String> fileKeys) {
        this.fileKeys = fileKeys;
    }

    public List<String> getFileKeys() {
        return fileKeys;
    }

    public void setFileKeys(List<String> fileKeys) {
        this.fileKeys = fileKeys;
    }

    @Override
    public String toString() {
        return "UploadThingDeleteRequest{" +
                "fileKeys=" + fileKeys +
                '}';
    }
}
