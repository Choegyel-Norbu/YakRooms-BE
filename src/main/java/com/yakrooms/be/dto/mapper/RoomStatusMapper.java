package com.yakrooms.be.dto.mapper;

import com.yakrooms.be.dto.RoomStatusDTO;
import com.yakrooms.be.projection.RoomStatusProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting RoomStatusProjection to RoomStatusDTO
 * Handles the conversion to avoid JPA proxy serialization issues
 */
@Component
public class RoomStatusMapper {

    /**
     * Convert a single RoomStatusProjection to RoomStatusDTO
     */
    public RoomStatusDTO toDto(RoomStatusProjection projection) {
        if (projection == null) {
            return null;
        }

        return new RoomStatusDTO(
            projection.getRoomNumber(),
            projection.getRoomType(),
            projection.getRoomStatus(),
            projection.getGuestName(),
            projection.getCheckOutDate()
        );
    }

    /**
     * Convert a list of RoomStatusProjection to list of RoomStatusDTO
     */
    public List<RoomStatusDTO> toDtoList(List<RoomStatusProjection> projections) {
        if (projections == null) {
            return null;
        }

        return projections.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert a Page of RoomStatusProjection to Page of RoomStatusDTO
     * Preserves pagination information
     */
    public Page<RoomStatusDTO> toDtoPage(Page<RoomStatusProjection> projectionPage) {
        if (projectionPage == null) {
            return null;
        }

        List<RoomStatusDTO> dtoList = projectionPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, projectionPage.getPageable(), projectionPage.getTotalElements());
    }
}
