package com.yakrooms.be.dto;

import java.math.BigDecimal;

public interface MonthlyRevenueStatsDTO {
    String getHotelName();
    String getMonthYear(); // Format: yyyy-MM
    BigDecimal getTotalRevenue();
    Long getBookingCount();
    BigDecimal getAverageBookingValue();
} 