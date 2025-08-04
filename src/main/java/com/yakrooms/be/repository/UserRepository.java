package com.yakrooms.be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic queries with index usage
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.isActive = true")
    boolean existsByEmailAndActive(@Param("email") String email);
    
    // Optimized query with specific role check
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId AND :role MEMBER OF u.roles")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<User> findByHotelIdAndRole(@Param("hotelId") Long hotelId, @Param("role") Role role);
    
    // Find all users by hotel with pagination
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId")
    Page<User> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);
    
    // Basic list query
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId")
    List<User> findByHotelId(@Param("hotelId") Long hotelId);
    
    // Optimized deletion - use deleteById instead
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId")
    void deleteUserById(@Param("userId") Long userId);
    
    // Fetch join queries for avoiding N+1
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "LEFT JOIN FETCH u.hotel h " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithCollections(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "LEFT JOIN FETCH u.hotel h " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithCollections(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "WHERE u.hotel.id = :hotelId")
    List<User> findByHotelIdWithRoles(@Param("hotelId") Long hotelId);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles " +
           "LEFT JOIN FETCH u.hotel h " +
           "WHERE u.hotel.id = :hotelId")
    List<User> findByHotelIdWithCollections(@Param("hotelId") Long hotelId);
    
    // Active user queries
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findAllActive(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId AND u.isActive = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<User> findActiveByHotelId(@Param("hotelId") Long hotelId);
    
    // Role-based queries
    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findByRole(@Param("role") Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId AND :role MEMBER OF u.roles AND u.isActive = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<User> findActiveByHotelIdAndRole(@Param("hotelId") Long hotelId, @Param("role") Role role);
    
    // Batch operations
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.hotel.id = :hotelId")
    int deactivateUsersByHotelId(@Param("hotelId") Long hotelId);
    
    @Modifying
    @Query("UPDATE User u SET u.hotel = null WHERE u.hotel.id = :hotelId")
    int disassociateUsersFromHotel(@Param("hotelId") Long hotelId);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);
    
    // Count queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.hotel.id = :hotelId")
    long countByHotelId(@Param("hotelId") Long hotelId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.hotel.id = :hotelId AND u.isActive = true")
    long countActiveByHotelId(@Param("hotelId") Long hotelId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE :role MEMBER OF u.roles")
    long countByRole(@Param("role") Role role);
    
    // Projection queries for performance
    @Query("SELECT u.id as id, u.name as name, u.email as email, u.isActive as isActive " +
           "FROM User u WHERE u.hotel.id = :hotelId")
    List<UserBasicProjection> findBasicUsersByHotelId(@Param("hotelId") Long hotelId);
    
    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.hotel.id = :hotelId AND " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersByHotel(@Param("hotelId") Long hotelId, @Param("search") String search, Pageable pageable);
    
    // Check if user has specific roles
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE u.id = :userId AND :role MEMBER OF u.roles")
    boolean hasRole(@Param("userId") Long userId, @Param("role") Role role);
    
    // Find users with any of the specified roles
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN :roles")
    List<User> findByRolesIn(@Param("roles") Set<Role> roles);
    
    // Native query for complex operations
    @Query(value = "SELECT u.* FROM users u " +
                   "JOIN user_roles ur ON u.id = ur.user_id " +
                   "WHERE ur.role IN :roles " +
                   "GROUP BY u.id " +
                   "HAVING COUNT(DISTINCT ur.role) = :roleCount",
           nativeQuery = true)
    List<User> findUsersWithAllRoles(@Param("roles") List<String> roles, @Param("roleCount") int roleCount);
    
    // Projection interface
    interface UserBasicProjection {
        Long getId();
        String getName();
        String getEmail();
        boolean getIsActive();
    }
}