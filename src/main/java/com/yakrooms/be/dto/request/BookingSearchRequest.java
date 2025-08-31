package com.yakrooms.be.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO for booking search requests with multiple search criteria.
 * Supports searching by CID, guest name, phone, check-in date, and combinations.
 */
public class BookingSearchRequest {
    
    @Size(max = 20, message = "CID must not exceed 20 characters")
    private String cid;
    
    @Size(max = 100, message = "Guest name must not exceed 100 characters")
    private String guestName;
    
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    
    private Long hotelId;
    
    private String status;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    
    // Search options
    private Boolean exactMatch = false;
    private Boolean includeInactive = false;
    
    // Constructors
    public BookingSearchRequest() {}
    
    public BookingSearchRequest(String cid, String guestName, String phone, 
                               LocalDate checkInDate, LocalDate checkOutDate, Long hotelId) {
        this.cid = cid;
        this.guestName = guestName;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.hotelId = hotelId;
    }
    
    // Getters and Setters
    public String getCid() {
        return cid;
    }
    
    public void setCid(String cid) {
        this.cid = cid;
    }
    
    public String getGuestName() {
        return guestName;
    }
    
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDate getCheckInDate() {
        return checkInDate;
    }
    
    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }
    
    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }
    
    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
    
    public Long getHotelId() {
        return hotelId;
    }
    
    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page != null ? page : 0;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size != null ? size : 10;
    }
    
    public Boolean getExactMatch() {
        return exactMatch;
    }
    
    public void setExactMatch(Boolean exactMatch) {
        this.exactMatch = exactMatch != null ? exactMatch : false;
    }
    
    public Boolean getIncludeInactive() {
        return includeInactive;
    }
    
    public void setIncludeInactive(Boolean includeInactive) {
        this.includeInactive = includeInactive != null ? includeInactive : false;
    }
    
    /**
     * Checks if this search request has any search criteria
     */
    public boolean hasSearchCriteria() {
        return cid != null || guestName != null || phone != null || 
               checkInDate != null || checkOutDate != null || status != null;
    }
    
    /**
     * Checks if this is a single-criteria search
     */
    public boolean isSingleCriteriaSearch() {
        int criteriaCount = 0;
        if (cid != null) criteriaCount++;
        if (guestName != null) criteriaCount++;
        if (phone != null) criteriaCount++;
        if (checkInDate != null) criteriaCount++;
        if (checkOutDate != null) criteriaCount++;
        if (status != null) criteriaCount++;
        return criteriaCount == 1;
    }
    
    @Override
    public String toString() {
        return "BookingSearchRequest{" +
                "cid='" + cid + '\'' +
                ", guestName='" + guestName + '\'' +
                ", phone='" + phone + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", hotelId=" + hotelId +
                ", status='" + status + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", exactMatch=" + exactMatch +
                ", includeInactive=" + includeInactive +
                '}';
    }
}
