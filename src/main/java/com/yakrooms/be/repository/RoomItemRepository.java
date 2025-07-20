package com.yakrooms.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.RoomItem;

@Repository
public interface RoomItemRepository extends JpaRepository<RoomItem, Long>{

}
