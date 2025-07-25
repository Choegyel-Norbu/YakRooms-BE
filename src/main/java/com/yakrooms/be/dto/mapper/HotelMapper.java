package com.yakrooms.be.dto.mapper;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.model.entity.Hotel;

@Component
public class HotelMapper {

    public HotelResponse toDto(Hotel hotel) {
        if (hotel == null) {
            return null;
        }

        HotelResponse response = new HotelResponse();
        response.setId(hotel.getId());
        response.setName(hotel.getName());
        response.setEmail(hotel.getEmail());
        response.setPhone(hotel.getPhone());
        response.setAddress(hotel.getAddress());
        response.setDistrict(hotel.getDistrict());
        response.setLogoUrl(hotel.getLogoUrl());
        response.setDescription(hotel.getDescription());
        response.setVerified(hotel.isVerified());
        response.setWebsiteUrl(hotel.getWebsiteUrl());
        response.setCreatedAt(hotel.getCreatedAt());
        response.setLicenseUrl(hotel.getLicenseUrl());
        response.setIdProofUrl(hotel.getIdProofUrl());
        response.setHotelType(hotel.getHotelType());
        
        // Copy lists (create new lists to avoid reference issues)
        if (hotel.getPhotoUrls() != null) {
            response.setPhotoUrls(new ArrayList<>(hotel.getPhotoUrls()));
        }
        
        if (hotel.getAmenities() != null) {
            response.setAmenities(new ArrayList<>(hotel.getAmenities()));
        }

        return response;
    }

    public Hotel toEntity(HotelRequest dto) {
        if (dto == null) {
            return null;
        }

        Hotel hotel = new Hotel();
        hotel.setName(dto.getName());
        hotel.setEmail(dto.getEmail());
        hotel.setPhone(dto.getPhone());
        hotel.setAddress(dto.getAddress());
        hotel.setDistrict(dto.getDistrict());
        hotel.setDescription(dto.getDescription());
        hotel.setWebsiteUrl(dto.getWebsiteUrl());
        hotel.setLicenseUrl(dto.getLicenseUrl());
        hotel.setIdProofUrl(dto.getIdProofUrl());
        hotel.setHotelType(dto.getHoteType()); // Note: using getHoteType() as per your HotelRequest
        
        // Copy lists (create new lists to avoid reference issues)
        if (dto.getPhotoUrls() != null) {
            hotel.setPhotoUrls(new ArrayList<>(dto.getPhotoUrls()));
        }
        
        if (dto.getAmenities() != null) {
            hotel.setAmenities(new ArrayList<>(dto.getAmenities()));
        }

        return hotel;
    }

    public void updateHotelFromRequest(HotelRequest dto, Hotel entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Update only non-null values (following IGNORE strategy)
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        
        if (dto.getAddress() != null) {
            entity.setAddress(dto.getAddress());
        }
        
        if (dto.getDistrict() != null) {
            entity.setDistrict(dto.getDistrict());
        }
        
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        
        if (dto.getWebsiteUrl() != null) {
            entity.setWebsiteUrl(dto.getWebsiteUrl());
        }
        
        if (dto.getLicenseUrl() != null) {
            entity.setLicenseUrl(dto.getLicenseUrl());
        }
        
        if (dto.getIdProofUrl() != null) {
            entity.setIdProofUrl(dto.getIdProofUrl());
        }
        
        if (dto.getHoteType() != null) {
            entity.setHotelType(dto.getHoteType());
        }
        
        // Update lists only if they are not null
        if (dto.getPhotoUrls() != null) {
            entity.setPhotoUrls(new ArrayList<>(dto.getPhotoUrls()));
        }
        
        if (dto.getAmenities() != null) {
            entity.setAmenities(new ArrayList<>(dto.getAmenities()));
        }
    }
}