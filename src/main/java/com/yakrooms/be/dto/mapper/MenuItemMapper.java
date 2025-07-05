package com.yakrooms.be.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yakrooms.be.dto.request.MenuItemRequest;
import com.yakrooms.be.dto.response.MenuItemResponse;
import com.yakrooms.be.model.entity.MenuItem;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MenuItemMapper {
    MenuItem toEntity(MenuItemRequest request);
    MenuItemResponse toDto(MenuItem item);
}