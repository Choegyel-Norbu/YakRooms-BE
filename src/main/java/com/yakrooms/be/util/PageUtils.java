package com.yakrooms.be.util;

import com.yakrooms.be.dto.response.PagedResponse;
import org.springframework.data.domain.Page;

/**
 * Utility class for pagination operations
 * Provides methods to convert Spring Page objects to stable PagedResponse DTOs
 */
public class PageUtils {
    
    private PageUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Converts a Spring Page to a PagedResponse with stable JSON structure
     * 
     * @param page The Spring Page object
     * @param <T> The content type
     * @return PagedResponse with stable structure
     */
    public static <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        if (page == null) {
            return new PagedResponse<>();
        }
        return new PagedResponse<>(page);
    }
    
    /**
     * Creates an empty PagedResponse
     * 
     * @param <T> The content type
     * @return Empty PagedResponse
     */
    public static <T> PagedResponse<T> emptyPagedResponse() {
        return new PagedResponse<>();
    }
}
