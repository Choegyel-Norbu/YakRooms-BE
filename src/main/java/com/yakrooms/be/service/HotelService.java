package com.yakrooms.be.service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.yakrooms.be.dto.HotelListingDto;
import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.request.HotelDeletionRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.dto.cache.HotelListingPageCacheDto;
import com.yakrooms.be.dto.cache.HotelSearchPageCacheDto;
import com.yakrooms.be.projection.HotelWithPriceProjection;

@Service
public interface HotelService {

	HotelResponse createHotel(HotelRequest request, Long userId);
	public HotelListingDto getListingForUser(Long userId);
    Page<HotelListingPageCacheDto> getAllHotels(Pageable pageable);
    HotelResponse updateHotel(Long id, HotelRequest request);
    HotelResponse getHotelById(Long hotelId);
    void deleteHotel(Long id);
    Map<String, Object> verifyHotel(Long id);
//    Page<HotelResponse> searchHotels(String district, String hotelType, int page, int size);
    Page<HotelSearchPageCacheDto> searchHotels(String district, String locality, String hotelType, int page, int size);
    List<HotelWithPriceProjection> getTopThreeHotels();
    public Page<HotelResponse> getAllHotelsForSuperAdmin(Pageable pageable);
    Page<HotelListingPageCacheDto> getAllHotelsSortedByLowestPrice(Pageable pageable);
    Page<HotelListingPageCacheDto> getAllHotelsSortedByHighestPrice(Pageable pageable);
    
    // Hotel deletion request methods
    Map<String, Object> requestHotelDeletion(HotelDeletionRequest request);
    Page<HotelResponse> getHotelsWithDeletionRequests(Pageable pageable);

}
