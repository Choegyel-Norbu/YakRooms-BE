package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.model.entity.Hotel;

@Mapper(componentModel = "spring")
public interface HotelMapper {

	HotelMapper INSTANCE = Mappers.getMapper(HotelMapper.class);

	HotelResponse toDto(Hotel hotel);
	
	Hotel toEntity(HotelRequest dto);
	
    void updateHotelFromRequest(HotelRequest dto, @MappingTarget Hotel entity);

}
