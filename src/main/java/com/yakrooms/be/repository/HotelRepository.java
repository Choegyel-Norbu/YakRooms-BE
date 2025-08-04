package com.yakrooms.be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.model.enums.HotelType;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;

import jakarta.persistence.QueryHint;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
    
    // Optimized with index usage
    boolean existsByEmail(String email);
    
    // Use projection for read-only operations
    @Query("SELECT h.id as id, h.name as name, h.email as email FROM Hotel h WHERE h.email = :email")
    Optional<HotelBasicProjection> findBasicByEmail(@Param("email") String email);
    
    // Fetch join to avoid N+1
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "LEFT JOIN FETCH h.amenities " +
           "LEFT JOIN FETCH h.photoUrls " +
           "WHERE h.id = :id")
    Optional<Hotel> findByIdWithCollections(@Param("id") Long id);
    
    // Optimized verified hotels query
    @Query("SELECT h FROM Hotel h WHERE h.isVerified = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Hotel> findAllVerified(Pageable pageable);
    
    // Optimized district search with index
    @Query("SELECT h FROM Hotel h WHERE h.district = :district AND h.isVerified = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Hotel> findVerifiedHotelsByDistrict(@Param("district") String district);
    
    // Optimized owner email query
    @Query("SELECT u.email FROM User u WHERE u.hotel.id = :hotelId AND :role MEMBER OF u.roles")
    Optional<String> findOwnerEmailByHotelId(@Param("hotelId") Long hotelId, @Param("role") com.yakrooms.be.model.enums.Role role);
    
    // Optimized hotel listing query using interface projection
    @Query(value = """
        SELECT 
            h.id as id,
            h.name as name,
            h.address as address,
            h.district as district,
            h.description as description,
            h.phone as phone,
            h.is_verified as isVerified,
            h.created_at as createdAt,
            h.hotel_type as hotelType,
            GROUP_CONCAT(DISTINCT hp.url) as photoUrls,
            GROUP_CONCAT(DISTINCT ha.amenity) as amenities
        FROM hotels h
        JOIN users u ON h.id = u.hotel_id AND u.id = :userId
        LEFT JOIN hotel_photo_urls hp ON hp.hotel_id = h.id
        LEFT JOIN hotel_amenities ha ON ha.hotel_id = h.id
        GROUP BY h.id
        """, nativeQuery = true)
    Optional<HotelListingProjection> findHotelListingByUserId(@Param("userId") Long userId);
    
    // Optimized search with proper index usage
    @Query("SELECT h FROM Hotel h WHERE " +
           "h.isVerified = true AND " +
           "(:district IS NULL OR h.district = :district) AND " +
           "(:hotelType IS NULL OR h.hotelType = :hotelType)")
    Page<Hotel> findByDistrictAndHotelType(@Param("district") String district, 
                                          @Param("hotelType") HotelType hotelType,
                                          Pageable pageable);
    
    // Optimized top 3 hotels query
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
            MIN(r.price) as lowest_price,
            GROUP_CONCAT(hp.url) as photo_urls
        FROM hotels h
        LEFT JOIN room r ON r.hotel_id = h.id
        LEFT JOIN hotel_photo_urls hp ON h.id = hp.hotel_id
        WHERE h.is_verified = 1
        GROUP BY h.id
        ORDER BY h.id
        LIMIT 3
        """, nativeQuery = true)
    List<HotelWithPriceProjection> findTop3VerifiedHotelsWithPhotosAndPrice();
    
    // Optimized paginated query with lowest price
    @Query(value = """
        SELECT 
            h.id as id,
            h.name as name,
            h.email as email,
            h.phone as phone,
            h.address as address,
            h.district as district,
            h.logo_url as logoUrl,
            h.description as description,
            h.is_verified as isVerified,
            h.website_url as websiteUrl,
            h.created_at as createdAt,
            h.license_url as licenseUrl,
            h.id_proof_url as idProofUrl,
            h.hotel_type as hotelType,
            COALESCE(rm.min_price, 0) as lowestPrice,
            hp.urls as photoUrls
        FROM hotels h
        LEFT JOIN (
            SELECT hotel_id, MIN(price) as min_price 
            FROM room 
            GROUP BY hotel_id
        ) rm ON rm.hotel_id = h.id
        LEFT JOIN (
            SELECT hotel_id, GROUP_CONCAT(url) as urls 
            FROM hotel_photo_urls 
            GROUP BY hotel_id
        ) hp ON hp.hotel_id = h.id
        WHERE h.is_verified = 1
        ORDER BY COALESCE(rm.min_price, 999999) ASC
        """, 
        countQuery = "SELECT COUNT(*) FROM hotels WHERE is_verified = 1", 
        nativeQuery = true)
    Page<HotelWithLowestPriceProjection> findAllVerifiedHotelsWithLowestPriceSorted(Pageable pageable);
    
    // Descending price sort
    @Query(value = """
        SELECT 
            h.id as id,
            h.name as name,
            h.email as email,
            h.phone as phone,
            h.address as address,
            h.district as district,
            h.logo_url as logoUrl,
            h.description as description,
            h.is_verified as isVerified,
            h.website_url as websiteUrl,
            h.created_at as createdAt,
            h.license_url as licenseUrl,
            h.id_proof_url as idProofUrl,
            h.hotel_type as hotelType,
            COALESCE(rm.min_price, 0) as lowestPrice,
            hp.urls as photoUrls
        FROM hotels h
        LEFT JOIN (
            SELECT hotel_id, MIN(price) as min_price 
            FROM room 
            GROUP BY hotel_id
        ) rm ON rm.hotel_id = h.id
        LEFT JOIN (
            SELECT hotel_id, GROUP_CONCAT(url) as urls 
            FROM hotel_photo_urls 
            GROUP BY hotel_id
        ) hp ON hp.hotel_id = h.id
        WHERE h.is_verified = 1
        ORDER BY COALESCE(rm.min_price, 0) DESC
        """, 
        countQuery = "SELECT COUNT(*) FROM hotels WHERE is_verified = 1", 
        nativeQuery = true)
    Page<HotelWithLowestPriceProjection> findAllVerifiedHotelsWithLowestPriceDesc(Pageable pageable);
    
    // Batch update for verification
    @Modifying
    @Query("UPDATE Hotel h SET h.isVerified = true WHERE h.id IN :ids")
    int verifyHotels(@Param("ids") List<Long> ids);
    
    // Exists check for verification status
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Hotel h WHERE h.id = :id AND h.isVerified = true")
    boolean isHotelVerified(@Param("id") Long id);
    
    // Basic projection interface
    interface HotelBasicProjection {
        Long getId();
        String getName();
        String getEmail();
    }
}