package com.yakrooms.be.util;

public class HotelSearchCriteria {
	private String location;
	private Double minPrice;
	private Double maxPrice;
	private Double minRating;
	private String keyword; // name, description match

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Double getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Double minPrice) {
		this.minPrice = minPrice;
	}

	public Double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(Double maxPrice) {
		this.maxPrice = maxPrice;
	}

	public Double getMinRating() {
		return minRating;
	}

	public void setMinRating(Double minRating) {
		this.minRating = minRating;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	// Add more fields as needed
}
