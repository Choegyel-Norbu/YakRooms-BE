package com.yakrooms.be.dto.cache;

import com.yakrooms.be.dto.cache.HotelWithLowestPriceCacheDto;

import java.util.List;

/**
 * Cache DTO for paginated hotel search results
 * Contains the content and pagination metadata for search queries
 */
public class HotelSearchPageCacheDto {
    
    private List<HotelWithLowestPriceCacheDto> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private boolean empty;
    private String searchKey; // district_locality_type for cache key generation
    
    // Default constructor
    public HotelSearchPageCacheDto() {}
    
    // Constructor with all fields
    public HotelSearchPageCacheDto(List<HotelWithLowestPriceCacheDto> content, 
                                   int pageNumber, 
                                   int pageSize, 
                                   long totalElements, 
                                   int totalPages, 
                                   boolean first, 
                                   boolean last, 
                                   int numberOfElements, 
                                   boolean empty, 
                                   String searchKey) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.numberOfElements = numberOfElements;
        this.empty = empty;
        this.searchKey = searchKey;
    }
    
    // Getters and Setters
    public List<HotelWithLowestPriceCacheDto> getContent() {
        return content;
    }
    
    public void setContent(List<HotelWithLowestPriceCacheDto> content) {
        this.content = content;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public int getNumberOfElements() {
        return numberOfElements;
    }
    
    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
    
    public String getSearchKey() {
        return searchKey;
    }
    
    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
    
    @Override
    public String toString() {
        return "HotelSearchPageCacheDto{" +
                "contentSize=" + (content != null ? content.size() : 0) +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                ", numberOfElements=" + numberOfElements +
                ", empty=" + empty +
                ", searchKey='" + searchKey + '\'' +
                '}';
    }
}
