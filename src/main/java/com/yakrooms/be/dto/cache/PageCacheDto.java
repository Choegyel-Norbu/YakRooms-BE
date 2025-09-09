package com.yakrooms.be.dto.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic cache DTO for paginated results - optimized for Redis serialization
 * Contains pagination metadata and content list without JPA/Hibernate dependencies
 */
public class PageCacheDto<T> {
    
    @JsonProperty("content")
    private List<T> content;
    
    @JsonProperty("pageNumber")
    private int pageNumber;
    
    @JsonProperty("pageSize")
    private int pageSize;
    
    @JsonProperty("totalElements")
    private long totalElements;
    
    @JsonProperty("totalPages")
    private int totalPages;
    
    @JsonProperty("first")
    private boolean first;
    
    @JsonProperty("last")
    private boolean last;
    
    @JsonProperty("numberOfElements")
    private int numberOfElements;
    
    @JsonProperty("empty")
    private boolean empty;

    // Default constructor for Jackson
    public PageCacheDto() {}

    // Constructor for easy creation
    public PageCacheDto(List<T> content, int pageNumber, int pageSize, long totalElements,
                       int totalPages, boolean first, boolean last, int numberOfElements, boolean empty) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.numberOfElements = numberOfElements;
        this.empty = empty;
    }

    // Getters and Setters
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public int getNumberOfElements() { return numberOfElements; }
    public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}
