package com.yakrooms.be.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.yakrooms.be.projection.HotelListingProjection;

public class HotelListingDto {
    private Long id;
    private String name;
    private String address;
    private String district;
    private String locality;
    private String description;
    private String phone;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private List<String> photoUrls;
    private List<String> amenities;
    private String hotelType;
    private LocalTime checkinTime;
    private LocalTime checkoutTime;
    
    public HotelListingDto() {
    }
    
    public HotelListingDto(Long id, String name, String address, String district, String locality,
                          String description, String phone, Boolean isVerified, 
                          LocalDateTime createdAt, List<String> photoUrls, 
                          List<String> amenities, String hotelType, LocalTime checkinTime, LocalTime checkoutTime) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.district = district;
        this.locality = locality;
        this.description = description;
        this.phone = phone;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.photoUrls = photoUrls;
        this.amenities = amenities;
        this.hotelType = hotelType;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
    }
    
    public static HotelListingDto fromProjection(HotelListingProjection projection) {
        HotelListingDto dto = new HotelListingDto();
        dto.setId(projection.getId());
        dto.setName(projection.getName());
        dto.setAddress(projection.getAddress());
        dto.setDistrict(projection.getDistrict());
        dto.setLocality(projection.getLocality());
        dto.setDescription(projection.getDescription());
        dto.setPhone(projection.getPhone());
        dto.setIsVerified(projection.getIsVerified());
        dto.setCreatedAt(projection.getCreatedAt());
        dto.setHotelType(projection.getHotelType());
        dto.setCheckinTime(projection.getCheckinTime());
        dto.setCheckoutTime(projection.getCheckoutTime());
        dto.setPhotoUrls(parseCommaSeparatedList(projection.getPhotoUrls()));
        dto.setAmenities(parseCommaSeparatedList(projection.getAmenities()));
        return dto;
    }
    
    private static List<String> parseCommaSeparatedList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getLocality() {
        return locality;
    }
    
    public void setLocality(String locality) {
        this.locality = locality;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<String> getPhotoUrls() {
        return photoUrls;
    }
    
    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
    
    public List<String> getAmenities() {
        return amenities;
    }
    
    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
    
    public String getHotelType() {
        return hotelType;
    }
    
    public void setHotelType(String hotelType) {
        this.hotelType = hotelType;
    }
    
    public LocalTime getCheckinTime() {
        return checkinTime;
    }
    
    public void setCheckinTime(LocalTime checkinTime) {
        this.checkinTime = checkinTime;
    }
    
    public LocalTime getCheckoutTime() {
        return checkoutTime;
    }
    
    public void setCheckoutTime(LocalTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotelListingDto that = (HotelListingDto) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "HotelListingDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", district='" + district + '\'' +
                ", hotelType='" + hotelType + '\'' +
                ", isVerified=" + isVerified +
                '}';
    }
}