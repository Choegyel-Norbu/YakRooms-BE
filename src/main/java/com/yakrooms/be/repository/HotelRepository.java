package com.yakrooms.be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.Hotel;
import com.yakrooms.be.projection.HotelListingProjection;
import com.yakrooms.be.projection.HotelWithCollectionsAndRatingProjection;
import com.yakrooms.be.projection.HotelWithLowestPriceProjection;
import com.yakrooms.be.projection.HotelWithPriceProjection;


@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    // Basic CRUD operations - optimized with index usage
    boolean existsByEmail(String email);

    // Hotel with collections and rating - optimized with proper joins and indexing
    @Query(value = """
            SELECT
                h.id as id,
                h.name as name,
                h.email as email,
                h.phone as phone,
                h.address as address,
                h.district as district,
                h.locality as locality,
                h.logo_url as logoUrl,
                h.description as description,
                h.is_verified as isVerified,
                h.website_url as websiteUrl,
                h.created_at as createdAt,
                h.updated_at as updatedAt,
                h.license_url as licenseUrl,
                h.id_proof_url as idProofUrl,
                h.latitude as latitude,
                h.longitude as longitude,
                h.hotel_type as hotelType,
                h.checkin_time as checkinTime,
                h.checkout_time as checkoutTime,
                h.deletion_requested as deletionRequested,
                h.deletion_reason as deletionReason,
                h.deletion_requested_at as deletionRequestedAt,
                GROUP_CONCAT(DISTINCT ha.amenity) as amenities,
                GROUP_CONCAT(DISTINCT hp.url) as photoUrls,
                SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT hp.url), ',', 1) as photoUrl,
                COALESCE(AVG(r.rating), 0) as averageRating
            FROM hotels h
            LEFT JOIN hotel_amenities ha ON ha.hotel_id = h.id
            LEFT JOIN hotel_photo_urls hp ON hp.hotel_id = h.id
            LEFT JOIN reviews r ON r.hotel_id = h.id
            WHERE h.id = :id
            GROUP BY h.id
            """, nativeQuery = true)
    Optional<HotelWithCollectionsAndRatingProjection> findByIdWithCollections(@Param("id") Long id);

    // Hotel listing by user ID - optimized with proper joins
    @Query(value = """
            SELECT
                h.id as id,
                h.name as name,
                h.address as address,
                h.district as district,
                h.locality as locality,
                h.description as description,
                h.phone as phone,
                h.is_verified as isVerified,
                h.created_at as createdAt,
                h.hotel_type as hotelType,
                h.checkin_time as checkinTime,
                h.checkout_time as checkoutTime,
                GROUP_CONCAT(DISTINCT hp.url) as photoUrls,
                SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT hp.url), ',', 1) as photoUrl,
                GROUP_CONCAT(DISTINCT ha.amenity) as amenities
            FROM hotels h
            JOIN users u ON h.id = u.hotel_id AND u.id = :userId
            LEFT JOIN hotel_photo_urls hp ON hp.hotel_id = h.id
            LEFT JOIN hotel_amenities ha ON ha.hotel_id = h.id
            GROUP BY h.id
            """, nativeQuery = true)
    Optional<HotelListingProjection> findHotelListingByUserId(@Param("userId") Long userId);
    
    // Search hotels with filters - optimized with composite index usage
    @Query(value = """
            SELECT
                h.id as id,
                h.name as name,
                h.email as email,
                h.phone as phone,
                h.address as address,
                h.district as district,
                h.locality as locality,
                h.logo_url as logoUrl,
                h.description as description,
                h.is_verified as isVerified,
                h.website_url as websiteUrl,
                h.created_at as createdAt,
                h.license_url as licenseUrl,
                h.id_proof_url as idProofUrl,
                h.hotel_type as hotelType,
                COALESCE(rm.min_price, 0) as lowestPrice,
                hp.urls as photoUrls,
                SUBSTRING_INDEX(hp.urls, ',', 1) as photoUrl,
                COALESCE(rv.avg_rating, 0) as averageRating
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
            LEFT JOIN (
                SELECT hotel_id, AVG(rating) as avg_rating
                FROM reviews
                GROUP BY hotel_id
            ) rv ON rv.hotel_id = h.id
            WHERE h.is_verified = 1
            AND (:district IS NULL OR LOWER(h.district) LIKE LOWER(CONCAT('%', :district, '%')))
            AND (:locality IS NULL OR LOWER(h.locality) LIKE LOWER(CONCAT('%', :locality, '%')))
            AND (:hotelType IS NULL OR h.hotel_type = :hotelType)
            ORDER BY COALESCE(rm.min_price, 999999) ASC
            """, 
            countQuery = """
            SELECT COUNT(*) 
            FROM hotels h 
            WHERE h.is_verified = 1
            AND (:district IS NULL OR LOWER(h.district) LIKE LOWER(CONCAT('%', :district, '%')))
            AND (:locality IS NULL OR LOWER(h.locality) LIKE LOWER(CONCAT('%', :locality, '%')))
            AND (:hotelType IS NULL OR h.hotel_type = :hotelType)
            """, 
            nativeQuery = true)
    Page<HotelWithLowestPriceProjection> findAllVerifiedHotelsWithLowestPriceSortedAndFiltered(
        @Param("district") String district,
        @Param("locality") String locality,
        @Param("hotelType") String hotelType,
        Pageable pageable
    );

    // Top 3 hotels by rating - optimized with proper indexing
    @Query(value = """
            SELECT
                h.id,
                h.name,
                h.email,
                h.phone,
                h.address,
                h.district,
                h.locality,
                h.logo_url,
                h.description,
                h.is_verified,
                h.website_url,
                h.created_at,
                h.license_url,
                h.id_proof_url,
                AVG(r2.rating) AS avg_rating,
                MIN(r.price) AS lowest_price,
                GROUP_CONCAT(hp.url) AS photo_urls,
                SUBSTRING_INDEX(GROUP_CONCAT(hp.url), ',', 1) AS photo_url
            FROM hotels h
            LEFT JOIN room r ON r.hotel_id = h.id
            LEFT JOIN hotel_photo_urls hp ON h.id = hp.hotel_id
            LEFT JOIN reviews r2 ON r2.hotel_id = h.id
            WHERE h.is_verified = 1
            GROUP BY h.id
            ORDER BY avg_rating DESC
            LIMIT 3
            """, nativeQuery = true)
    List<HotelWithPriceProjection> findTop3VerifiedHotelsWithPhotosAndPrice();

    // All verified hotels with lowest price - ascending order
    @Query(value = """
            SELECT
                h.id as id,
                h.name as name,
                h.email as email,
                h.phone as phone,
                h.address as address,
                h.district as district,
                h.locality as locality,
                h.logo_url as logoUrl,
                h.description as description,
                h.is_verified as isVerified,
                h.website_url as websiteUrl,
                h.created_at as createdAt,
                h.license_url as licenseUrl,
                h.id_proof_url as idProofUrl,
                h.hotel_type as hotelType,
                COALESCE(rm.min_price, 0) as lowestPrice,
                hp.urls as photoUrls,
                SUBSTRING_INDEX(hp.urls, ',', 1) as photoUrl,
                COALESCE(rv.avg_rating, 0) as averageRating
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
            LEFT JOIN (
                SELECT hotel_id, AVG(rating) as avg_rating
                FROM reviews
                GROUP BY hotel_id
            ) rv ON rv.hotel_id = h.id
            WHERE h.is_verified = 1
            ORDER BY COALESCE(rm.min_price, 999999) ASC
            """, 
            countQuery = "SELECT COUNT(*) FROM hotels WHERE is_verified = 1", 
            nativeQuery = true)
    Page<HotelWithLowestPriceProjection> findAllVerifiedHotelsWithLowestPriceSorted(Pageable pageable);

    // All verified hotels with lowest price - descending order
    @Query(value = """
            SELECT
                h.id as id,
                h.name as name,
                h.email as email,
                h.phone as phone,
                h.address as address,
                h.district as district,
                h.locality as locality,
                h.logo_url as logoUrl,
                h.description as description,
                h.is_verified as isVerified,
                h.website_url as websiteUrl,
                h.created_at as createdAt,
                h.license_url as licenseUrl,
                h.id_proof_url as idProofUrl,
                h.hotel_type as hotelType,
                COALESCE(rm.min_price, 0) as lowestPrice,
                hp.urls as photoUrls,
                SUBSTRING_INDEX(hp.urls, ',', 1) as photoUrl,
                COALESCE(rv.avg_rating, 0) as averageRating
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
            LEFT JOIN (
                SELECT hotel_id, AVG(rating) as avg_rating
                FROM reviews
                GROUP BY hotel_id
            ) rv ON rv.hotel_id = h.id
            WHERE h.is_verified = 1
            ORDER BY COALESCE(rm.min_price, 0) DESC
            """, 
            countQuery = "SELECT COUNT(*) FROM hotels WHERE is_verified = 1", 
            nativeQuery = true)
    Page<HotelWithLowestPriceProjection> findAllVerifiedHotelsWithLowestPriceDesc(Pageable pageable);

    // Verification status check - optimized with index
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Hotel h WHERE h.id = :id AND h.isVerified = true")
    boolean isHotelVerified(@Param("id") Long id);

    // Find hotels with deletion requests - optimized with index
    Page<Hotel> findByDeletionRequestedTrue(Pageable pageable);
}