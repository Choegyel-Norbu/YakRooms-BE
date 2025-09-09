package com.yakrooms.be.dto.mapper;

import com.yakrooms.be.dto.cache.*;
import com.yakrooms.be.dto.response.HotelListingResponseDto;
import com.yakrooms.be.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between entities/projections and cache DTOs
 * Provides clean separation between JPA entities and Redis cache storage
 */
@Component
public class CacheMapper {

    /**
     * Convert HotelWithCollectionsAndRatingProjection to HotelDetailsCacheDto
     */
    public HotelDetailsCacheDto toHotelDetailsCacheDto(HotelWithCollectionsAndRatingProjection projection) {
        if (projection == null) {
            return null;
        }

        return new HotelDetailsCacheDto(
            projection.getId(),
            projection.getName(),
            projection.getEmail(),
            projection.getPhone(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getLogoUrl(),
            projection.getDescription(),
            projection.getIsVerified(),
            projection.getWebsiteUrl(),
            projection.getCreatedAt(),
            projection.getLicenseUrl(),
            projection.getIdProofUrl(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getHotelType(),
            projection.getAmenities() != null ? List.copyOf(projection.getAmenities()) : List.of(),
            projection.getPhotoUrls() != null ? List.copyOf(projection.getPhotoUrls()) : List.of(),
            projection.getAverageRating()
        );
    }

    /**
     * Convert HotelWithLowestPriceProjection to HotelListingCacheDto
     */
    public HotelListingCacheDto toHotelListingCacheDto(HotelWithLowestPriceProjection projection) {
        if (projection == null) {
            return null;
        }

        return new HotelListingCacheDto(
            projection.getId(),
            projection.getName(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getDescription(),
            projection.getPhone(),
            projection.getIsVerified(),
            projection.getCreatedAt(),
            projection.getPhotoUrls() != null ? List.copyOf(projection.getPhotoUrls()) : List.of(),
            projection.getAmenities() != null ? List.copyOf(projection.getAmenities()) : List.of(),
            projection.getHotelType(),
            projection.getLowestPrice(),
            projection.getAverageRating()
        );
    }

    /**
     * Convert HotelWithLowestPriceProjection to HotelSearchCacheDto
     */
    public HotelSearchCacheDto toHotelSearchCacheDto(HotelWithLowestPriceProjection projection) {
        if (projection == null) {
            return null;
        }

        return new HotelSearchCacheDto(
            projection.getId(),
            projection.getName(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getDescription(),
            projection.getPhone(),
            projection.getIsVerified(),
            projection.getCreatedAt(),
            projection.getPhotoUrls() != null ? List.copyOf(projection.getPhotoUrls()) : List.of(),
            projection.getAmenities() != null ? List.copyOf(projection.getAmenities()) : List.of(),
            projection.getHotelType(),
            projection.getLowestPrice(),
            projection.getAverageRating()
        );
    }

    /**
     * Convert HotelWithPriceProjection to HotelTopCacheDto
     */
    public HotelTopCacheDto toHotelTopCacheDto(HotelWithPriceProjection projection) {
        if (projection == null) {
            return null;
        }

        return new HotelTopCacheDto(
            projection.getId(),
            projection.getName(),
            projection.getEmail(),
            projection.getPhone(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getLogoUrl(),
            projection.getDescription(),
            projection.getIsVerified(),
            projection.getWebsiteUrl(),
            projection.getCreatedAt(),
            projection.getLicenseUrl(),
            projection.getIdProofUrl(),
            null, // HotelType not available in HotelWithPriceProjection
            projection.getLowestPrice(),
            projection.getPhotoUrls(),
            projection.getPhotoUrl(),
            projection.getAvgRating()
        );
    }

    /**
     * Convert HotelListingProjection to HotelListingCacheDto
     */
    public HotelListingCacheDto toHotelListingCacheDto(HotelListingProjection projection) {
        if (projection == null) {
            return null;
        }

        return new HotelListingCacheDto(
            projection.getId(),
            projection.getName(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getDescription(),
            projection.getPhone(),
            projection.getIsVerified(),
            projection.getCreatedAt(),
            parseCommaSeparatedList(projection.getPhotoUrls()),
            parseCommaSeparatedList(projection.getAmenities()),
            parseHotelType(projection.getHotelType()),
            null, // No price in HotelListingProjection
            null  // No rating in HotelListingProjection
        );
    }

    /**
     * Convert Page<HotelWithLowestPriceProjection> to PageCacheDto<HotelListingCacheDto>
     */
    public PageCacheDto<HotelListingCacheDto> toPageCacheDto(Page<HotelWithLowestPriceProjection> page) {
        if (page == null) {
            return null;
        }

        List<HotelListingCacheDto> content = page.getContent().stream()
            .map(this::toHotelListingCacheDto)
            .collect(Collectors.toList());

        return new PageCacheDto<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.getNumberOfElements(),
            page.isEmpty()
        );
    }

    /**
     * Convert Page<HotelWithLowestPriceProjection> to PageCacheDto<HotelSearchCacheDto>
     */
    public PageCacheDto<HotelSearchCacheDto> toSearchPageCacheDto(Page<HotelWithLowestPriceProjection> page) {
        if (page == null) {
            return null;
        }

        List<HotelSearchCacheDto> content = page.getContent().stream()
            .map(this::toHotelSearchCacheDto)
            .collect(Collectors.toList());

        return new PageCacheDto<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.getNumberOfElements(),
            page.isEmpty()
        );
    }

    /**
     * Convert List<HotelWithPriceProjection> to List<HotelTopCacheDto>
     */
    public List<HotelTopCacheDto> toHotelTopCacheDtoList(List<HotelWithPriceProjection> projections) {
        if (projections == null) {
            return List.of();
        }

        return projections.stream()
            .map(this::toHotelTopCacheDto)
            .collect(Collectors.toList());
    }

    /**
     * Convert List<HotelListingProjection> to List<HotelListingCacheDto>
     */
    public List<HotelListingCacheDto> toHotelListingCacheDtoList(List<HotelListingProjection> projections) {
        if (projections == null) {
            return List.of();
        }

        return projections.stream()
            .map(this::toHotelListingCacheDto)
            .collect(Collectors.toList());
    }

    // Helper methods for parsing comma-separated strings
    private List<String> parseCommaSeparatedList(String commaSeparatedString) {
        if (commaSeparatedString == null || commaSeparatedString.trim().isEmpty()) {
            return List.of();
        }
        return List.of(commaSeparatedString.split(","))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private com.yakrooms.be.model.enums.HotelType parseHotelType(String hotelTypeString) {
        if (hotelTypeString == null || hotelTypeString.trim().isEmpty()) {
            return null;
        }
        try {
            return com.yakrooms.be.model.enums.HotelType.valueOf(hotelTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // New mapping methods for DTO-based paginated caching

    /**
     * Convert HotelWithLowestPriceProjection to HotelWithLowestPriceCacheDto
     */
    public HotelWithLowestPriceCacheDto toHotelWithLowestPriceCacheDto(HotelWithLowestPriceProjection projection) {
        if (projection == null) {
            return null;
        }

        return new HotelWithLowestPriceCacheDto(
            projection.getId(),
            projection.getName(),
            projection.getEmail(),
            projection.getPhone(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getLogoUrl(),
            projection.getDescription(),
            projection.getIsVerified(),
            projection.getWebsiteUrl(),
            projection.getCreatedAt(),
            projection.getPhotoUrls(),
            projection.getLicenseUrl(),
            projection.getIdProofUrl(),
            projection.getAmenities(),
            projection.getHotelType(),
            projection.getLowestPrice(),
            projection.getPhotoUrl(),
            projection.getAverageRating()
        );
    }

    /**
     * Convert List<HotelWithLowestPriceProjection> to List<HotelWithLowestPriceCacheDto>
     */
    public List<HotelWithLowestPriceCacheDto> toHotelWithLowestPriceCacheDtoList(List<HotelWithLowestPriceProjection> projections) {
        if (projections == null) {
            return List.of();
        }

        return projections.stream()
            .map(this::toHotelWithLowestPriceCacheDto)
            .collect(Collectors.toList());
    }

    /**
     * Convert HotelWithLowestPriceCacheDto back to HotelWithLowestPriceProjection
     * Uses concrete implementation instead of anonymous classes to avoid Jackson serialization issues
     */
    public HotelWithLowestPriceProjection toHotelWithLowestPriceProjection(HotelWithLowestPriceCacheDto dto) {
        if (dto == null) {
            return null;
        }

        // Create concrete implementation to avoid Jackson proxy serialization issues
        return new com.yakrooms.be.projection.impl.HotelWithLowestPriceProjectionImpl(
            dto.getId(),
            dto.getName(),
            dto.getEmail(),
            dto.getPhone(),
            dto.getAddress(),
            dto.getDistrict(),
            dto.getLocality(),
            dto.getLogoUrl(),
            dto.getDescription(),
            dto.isVerified(),
            dto.getWebsiteUrl(),
            dto.getCreatedAt(),
            dto.getPhotoUrls(),
            dto.getLicenseUrl(),
            dto.getIdProofUrl(),
            dto.getAmenities(),
            dto.getHotelType(),
            dto.getLowestPrice(),
            dto.getPhotoUrl(),
            dto.getAverageRating()
        );
    }

    /**
     * Convert List<HotelWithLowestPriceCacheDto> to List<HotelWithLowestPriceProjection>
     */
    public List<HotelWithLowestPriceProjection> toHotelWithLowestPriceProjectionList(List<HotelWithLowestPriceCacheDto> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
            .map(this::toHotelWithLowestPriceProjection)
            .collect(Collectors.toList());
    }

    /**
     * Convert Page<HotelWithLowestPriceProjection> to HotelSearchPageCacheDto
     */
    public HotelSearchPageCacheDto toHotelSearchPageCacheDto(Page<HotelWithLowestPriceProjection> page, 
                                                           String district, String locality, String hotelType) {
        if (page == null) {
            return null;
        }

        String searchKey = String.format("%s_%s_%s", 
            district != null ? district : "null",
            locality != null ? locality : "null", 
            hotelType != null ? hotelType : "null");

        return new HotelSearchPageCacheDto(
            toHotelWithLowestPriceCacheDtoList(page.getContent()),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.getNumberOfElements(),
            page.isEmpty(),
            searchKey
        );
    }

    /**
     * Convert HotelSearchPageCacheDto back to Page<HotelWithLowestPriceProjection>
     */
    public Page<HotelWithLowestPriceProjection> toPageFromHotelSearchPageCacheDto(HotelSearchPageCacheDto cacheDto) {
        if (cacheDto == null) {
            return Page.empty();
        }

        // Create PageImpl from cache data
        return new PageImpl<>(
            toHotelWithLowestPriceProjectionList(cacheDto.getContent()),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }

    /**
     * Convert Page<HotelWithLowestPriceProjection> to HotelListingPageCacheDto
     */
    public HotelListingPageCacheDto toHotelListingPageCacheDto(Page<HotelWithLowestPriceProjection> page, 
                                                             String sortType) {
        if (page == null) {
            return null;
        }

        return new HotelListingPageCacheDto(
            toHotelWithLowestPriceCacheDtoList(page.getContent()),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.getNumberOfElements(),
            page.isEmpty(),
            sortType
        );
    }

    /**
     * Convert HotelListingPageCacheDto back to Page<HotelWithLowestPriceProjection>
     */
    public Page<HotelWithLowestPriceProjection> toPageFromHotelListingPageCacheDto(HotelListingPageCacheDto cacheDto) {
        if (cacheDto == null) {
            return Page.empty();
        }

        // Create PageImpl from cache data
        return new PageImpl<>(
            toHotelWithLowestPriceProjectionList(cacheDto.getContent()),
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }
    
    /**
     * Convert HotelListingPageCacheDto to Page<HotelListingResponseDto>
     * This method returns clean DTOs that can be safely serialized without proxy issues
     */
    public Page<HotelListingResponseDto> toCleanPageFromHotelListingPageCacheDto(HotelListingPageCacheDto cacheDto) {
        if (cacheDto == null) {
            return Page.empty();
        }

        List<HotelListingResponseDto> cleanDtos = cacheDto.getContent().stream()
            .map(this::toHotelListingResponseDto)
            .collect(Collectors.toList());

        return new PageImpl<>(
            cleanDtos,
            PageRequest.of(cacheDto.getPageNumber(), cacheDto.getPageSize()),
            cacheDto.getTotalElements()
        );
    }
    
    /**
     * Convert HotelWithLowestPriceCacheDto to HotelListingResponseDto
     */
    public HotelListingResponseDto toHotelListingResponseDto(HotelWithLowestPriceCacheDto cacheDto) {
        if (cacheDto == null) {
            return null;
        }
        
        return new HotelListingResponseDto(
            cacheDto.getId(),
            cacheDto.getName(),
            cacheDto.getEmail(),
            cacheDto.getPhone(),
            cacheDto.getAddress(),
            cacheDto.getDistrict(),
            cacheDto.getLocality(),
            cacheDto.getLogoUrl(),
            cacheDto.getDescription(),
            cacheDto.isVerified(),
            cacheDto.getWebsiteUrl(),
            cacheDto.getCreatedAt(),
            cacheDto.getPhotoUrls(),
            cacheDto.getLicenseUrl(),
            cacheDto.getIdProofUrl(),
            cacheDto.getAmenities(),
            cacheDto.getHotelType(),
            cacheDto.getLowestPrice(),
            cacheDto.getPhotoUrl(),
            cacheDto.getAverageRating()
        );
    }
    
    /**
     * Convert HotelWithLowestPriceProjection to HotelListingResponseDto
     */
    public HotelListingResponseDto toHotelListingResponseDto(HotelWithLowestPriceProjection projection) {
        if (projection == null) {
            return null;
        }
        
        return new HotelListingResponseDto(
            projection.getId(),
            projection.getName(),
            projection.getEmail(),
            projection.getPhone(),
            projection.getAddress(),
            projection.getDistrict(),
            projection.getLocality(),
            projection.getLogoUrl(),
            projection.getDescription(),
            projection.getIsVerified(),
            projection.getWebsiteUrl(),
            projection.getCreatedAt(),
            projection.getPhotoUrls(),
            projection.getLicenseUrl(),
            projection.getIdProofUrl(),
            projection.getAmenities(),
            projection.getHotelType(),
            projection.getLowestPrice(),
            projection.getPhotoUrl(),
            projection.getAverageRating()
        );
    }
}
