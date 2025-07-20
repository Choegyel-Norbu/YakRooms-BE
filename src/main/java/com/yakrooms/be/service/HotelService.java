package com.yakrooms.be.service;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;
import com.yakrooms.be.util.HotelSearchCriteria;

@Service
public interface HotelService {

	HotelResponse createHotel(HotelRequest request, Long userId);
	public HotelListingDto getListingForUser(Long userId);
    Page<HotelResponse> getAllHotels(Pageable pageable);
    HotelResponse updateHotel(Long id, HotelRequest request);
    HotelResponse getHotelById(Long hotelId);
    void deleteHotel(Long id);
    void verifyHotel(Long id);
    List<HotelResponse> searchHotels(HotelSearchCriteria criteria);
    Page<HotelResponse> searchHotels(String district, String hotelType, int page, int size);
    List<HotelWithPriceProjection> getTopThreeHotels();
    public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable);

}
