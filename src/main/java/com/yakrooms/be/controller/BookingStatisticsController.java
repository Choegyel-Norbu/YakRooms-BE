package com.yakrooms.be.controller;

import com.yakrooms.be.dto.BookingStatisticsDTO;
import com.yakrooms.be.dto.MonthlyRevenueStatsDTO;
import com.yakrooms.be.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-statistics")
public class BookingStatisticsController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/monthly")
    public ResponseEntity<List<BookingStatisticsDTO>> getBookingStatisticsByMonth(
            @RequestParam String startDate) {
        try {
            List<BookingStatisticsDTO> statistics = bookingService.getBookingStatisticsByMonth(startDate);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/monthly/hotel/{hotelId}")
    public ResponseEntity<List<BookingStatisticsDTO>> getBookingStatisticsByMonthAndHotel(
            @PathVariable Long hotelId,
            @RequestParam String startDate) {
        try {
            List<BookingStatisticsDTO> statistics = bookingService.getBookingStatisticsByMonthAndHotel(startDate, hotelId);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revenue/monthly/{hotelId}")
    public ResponseEntity<List<MonthlyRevenueStatsDTO>> getMonthlyRevenueStats(
            @PathVariable Long hotelId,
            @RequestParam String startDate) {
        try {
            List<MonthlyRevenueStatsDTO> revenueStats = bookingService.getMonthlyRevenueStats(hotelId, startDate);
            return ResponseEntity.ok(revenueStats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 