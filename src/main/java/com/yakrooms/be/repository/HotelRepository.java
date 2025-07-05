package com.yakrooms.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.google.common.base.Optional;
import com.yakrooms.be.model.entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
	boolean existsByEmail(String email);

	Optional<Hotel> findByEmail(String email);

	@Query("SELECT h FROM Hotel h WHERE h.district = :district AND h.isVerified = true")
	List<Hotel> findVerifiedHotelsByDistrict(String district);

}
