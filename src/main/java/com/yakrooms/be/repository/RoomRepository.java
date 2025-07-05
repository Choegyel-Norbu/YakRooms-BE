package com.yakrooms.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

	List<Room> findAllByHotelId(Long hotelId);

}
