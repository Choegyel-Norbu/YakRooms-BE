package com.yakrooms.be.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        // Most common query: get reviews by hotel (with pagination/sorting by date)
        @Index(name = "idx_hotel_created_at", columnList = "hotel_id, created_at DESC"),

        // Get reviews by user (user's review history)
        @Index(name = "idx_user_created_at", columnList = "user_id, created_at DESC"),

        // Get reviews by rating for filtering (e.g., 5-star reviews)
        @Index(name = "idx_hotel_rating", columnList = "hotel_id, rating"),

        // Composite index for complex queries (hotel reviews with rating filter)
        @Index(name = "idx_hotel_rating_created", columnList = "hotel_id, rating, created_at DESC"),

        // Prevent duplicate reviews from same user for same hotel
        @Index(name = "idx_user_hotel_unique", columnList = "user_id, hotel_id", unique = true)
})
@NamedEntityGraph(
    name = "Review.withUser",
    attributeNodes = @NamedAttributeNode("user")
)
@NamedEntityGraph(
    name = "Review.withUserAndHotel", 
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("hotel")
    }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Column(length = 1000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_hotel"))
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_user"))
    @NotNull
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public Review() {
    }

    // Constructor for easier object creation
    public Review(Integer rating, String comment, Hotel hotel, User user) {
        this.rating = rating;
        this.comment = comment;
        this.hotel = hotel;
        this.user = user;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public boolean isActive() {
        return true; // All reviews are active since we removed soft delete
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review))
            return false;
        Review review = (Review) o;
        return id != null && id.equals(review.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // toString
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", hotel=" + (hotel != null ? hotel.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}