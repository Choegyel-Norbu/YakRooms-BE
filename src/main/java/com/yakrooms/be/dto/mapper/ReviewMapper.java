package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yakrooms.be.dto.request.ReviewRequest;
import com.yakrooms.be.dto.response.ReviewResponse;
import com.yakrooms.be.model.entity.Review;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReviewMapper {
    Review toEntity(ReviewRequest request);
    ReviewResponse toDto(Review review);
}