package com.yakrooms.be.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.google.common.base.Optional;
import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.projection.HotelWithPriceProjection;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
	boolean existsByEmail(String email);

	Page<Hotel> findAllByIsVerifiedTrue(Pageable pageable);

	Optional<Hotel> findByEmail(String email);

	@Query("SELECT h FROM Hotel h WHERE h.district = :district AND h.isVerified = true")
	List<Hotel> findVerifiedHotelsByDistrict(String district);

	@Query("SELECT h FROM Hotel h WHERE h.isVerified = true")
	Page<Hotel> findAllVerified(Pageable pageable);

	@Query("SELECT u.email FROM User u WHERE u.hotel.id = :hotelId AND u.role = 'HOTEL_ADMIN'")
	String findOwnerEmailByHotelId(@Param("hotelId") Long hotelId);

	@Query(value = "SELECT \n" + "    h.id,\n" + "    h.name,\n" + "    h.address,\n" + "    h.district,\n"
			+ "    h.description,\n" + "    h.phone,\n" + "    h.is_verified,\n" + "    h.created_at,\n"
			+ "    h.hotel_type ,\n" + "    GROUP_CONCAT(DISTINCT hpu.url) AS photoUrls,\n"
			+ "    GROUP_CONCAT(DISTINCT ha.amenity) AS amenities\n" + "FROM \n" + "    hotels h\n" + "JOIN \n"
			+ "    users u ON h.id = u.hotel_id\n" + "LEFT JOIN \n"
			+ "    hotel_photo_urls hpu ON hpu.hotel_id = h.id\n" + "LEFT JOIN \n"
			+ "    hotel_amenities ha ON ha.hotel_id = h.id\n" + "WHERE \n" + "    u.id = :userId\n" + "GROUP BY \n"
			+ "    h.id, h.name, h.address, h.district, h.description, h.phone, h.is_verified, h.created_at;\n"
			+ "", nativeQuery = true)
	List<Object[]> findRawHotelListingByUserId(@Param("userId") Long userId);

	@Query("SELECT h FROM Hotel h WHERE "
			+ "(:district IS NULL OR LOWER(h.district) LIKE LOWER(CONCAT('%', :district, '%'))) AND "
			+ "(:hotelType IS NULL OR LOWER(h.hotelType) = LOWER(:hotelType))")
	Page<Hotel> findByDistrictAndHotelType(@Param("district") String district, @Param("hotelType") String hotelType,
			Pageable pageable);

	@Query(value = """
					    SELECT
			    h.id,
			    h.name,
			    h.email,
			    h.phone,
			    h.address,
			    h.district,
			    h.logo_url,
			    h.description,
			    h.is_verified,
			    h.website_url,
			    h.created_at,
			    h.license_url,
			    h.id_proof_url,
			    (SELECT MIN(r.price) FROM room r WHERE r.hotel_id = h.id) AS lowest_price,
			    GROUP_CONCAT(hpu.url) AS photo_urls
			FROM hotels h
			LEFT JOIN hotel_photo_urls hpu ON h.id = hpu.hotel_id
			GROUP BY h.id
			ORDER BY h.id ASC
			LIMIT 3;
					    """, nativeQuery = true)
	List<HotelWithPriceProjection> findTop3HotelsWithPhotosAndPrice();

}
