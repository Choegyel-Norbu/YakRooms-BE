package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yakrooms.be.dto.request.RestaurantRequest;
import com.yakrooms.be.dto.response.RestaurantResponse;
import com.yakrooms.be.model.entity.Restaurant;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RestaurantMapper {
    Restaurant toEntity(RestaurantRequest request);
    RestaurantResponse toDto(Restaurant restaurant);
    void updateFromRequest(RestaurantRequest request, @MappingTarget Restaurant restaurant);

}