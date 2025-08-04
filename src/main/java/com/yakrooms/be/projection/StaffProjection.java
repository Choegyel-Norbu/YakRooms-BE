package com.yakrooms.be.projection;

import java.time.LocalDate;

public interface StaffProjection {
    Long getStaffId();
    Long getHotelId();
    String getStaffEmail();
    String getFullName();
    String getPosition();
    String getPhoneNumber();
    String getRoles();
    String getProfilePictureUrl();
    LocalDate getDateJoined();
} 