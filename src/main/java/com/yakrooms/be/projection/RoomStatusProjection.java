package com.yakrooms.be.projection;

import java.time.LocalDate;

public interface RoomStatusProjection {
	String getRoomNumber();
    String getRoomType();
    String getRoomStatus();
    String getGuestName();
    LocalDate getCheckOutDate(); 

}
