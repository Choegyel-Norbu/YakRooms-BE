package com.yakrooms.be.dto;

import java.math.BigDecimal;

/**
 * DTO for monthly revenue statistics
 * Converted from interface to class to avoid JPA proxy serialization issues
 */
public class MonthlyRevenueStatsDTO {
    private String hotelName;
    private String monthYear; // Format: yyyy-MM
    private BigDecimal totalRevenue;
    private Long bookingCount;
    private BigDecimal averageBookingValue;

    // Default constructor
    public MonthlyRevenueStatsDTO() {}

    // Constructor for all fields
    public MonthlyRevenueStatsDTO(String hotelName, String monthYear, BigDecimal totalRevenue, 
                                 Long bookingCount, BigDecimal averageBookingValue) {
        this.hotelName = hotelName;
        this.monthYear = monthYear;
        this.totalRevenue = totalRevenue;
        this.bookingCount = bookingCount;
        this.averageBookingValue = averageBookingValue;
    }

    // Getters and Setters
    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Long bookingCount) {
        this.bookingCount = bookingCount;
    }

    public BigDecimal getAverageBookingValue() {
        return averageBookingValue;
    }

    public void setAverageBookingValue(BigDecimal averageBookingValue) {
        this.averageBookingValue = averageBookingValue;
    }

    @Override
    public String toString() {
        return "MonthlyRevenueStatsDTO{" +
                "hotelName='" + hotelName + '\'' +
                ", monthYear='" + monthYear + '\'' +
                ", totalRevenue=" + totalRevenue +
                ", bookingCount=" + bookingCount +
                ", averageBookingValue=" + averageBookingValue +
                '}';
    }
} 