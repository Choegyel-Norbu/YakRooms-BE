package com.yakrooms.be.service;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.util.HotelSearchCriteria;

@Service
public interface HotelService {

	HotelResponse createHotel(HotelRequest request);
    HotelResponse getHotelById(Long id);
    Page<HotelResponse> getAllHotels(Pageable pageable);
    HotelResponse updateHotel(Long id, HotelRequest request);
    void deleteHotel(Long id);
    void verifyHotel(Long id);
    List<HotelResponse> searchHotels(HotelSearchCriteria criteria);
}
