package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import com.yakrooms.be.dto.request.HotelRequest;
import com.yakrooms.be.dto.response.HotelResponse;
import com.yakrooms.be.model.entity.Hotel;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HotelMapper {

	HotelResponse toDto(Hotel hotel);

	Hotel toEntity(HotelRequest dto);

	void updateHotelFromRequest(HotelRequest dto, @MappingTarget Hotel entity);
}
