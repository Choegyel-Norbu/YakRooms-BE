package com.yakrooms.be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for hotel deletion request from frontend.
 * Contains the reason for deletion to provide feedback to admin.
 */
public class HotelDeletionRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Deletion reason is required")
    @Size(min = 10, max = 1000, message = "Deletion reason must be between 10 and 1000 characters")
    private String deletionReason;

    // Constructors
    public HotelDeletionRequest() {}

    public HotelDeletionRequest(Long hotelId, String deletionReason) {
        this.hotelId = hotelId;
        this.deletionReason = deletionReason;
    }

    // Getters and Setters
    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    @Override
    public String toString() {
        return "HotelDeletionRequest{" +
                "hotelId=" + hotelId +
                ", deletionReason='" + deletionReason + '\'' +
                '}';
    }
}
