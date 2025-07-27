package com.yakrooms.be.dto;

public class BookingStatisticsDTO {
    private String monthYear;
    private Long bookingCount;

    public BookingStatisticsDTO() {}

    public BookingStatisticsDTO(String monthYear, Long bookingCount) {
        this.monthYear = monthYear;
        this.bookingCount = bookingCount;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public Long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Long bookingCount) {
        this.bookingCount = bookingCount;
    }
} 