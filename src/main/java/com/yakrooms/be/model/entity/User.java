package com.yakrooms.be.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;

import com.yakrooms.be.model.enums.Role;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_hotel_id", columnList = "hotel_id"),
    @Index(name = "idx_user_active", columnList = "is_active"),
    @Index(name = "idx_user_email_active", columnList = "email,is_active")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    private String password;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_pic_url", length = 500)
    private String profilePicUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"),
        indexes = @Index(name = "idx_user_role_user_id", columnList = "user_id")
    )
    @Column(name = "role", length = 50)
    @BatchSize(size = 20)
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Staff staff;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public User() {
        this.roles = new HashSet<>();
        this.isActive = true;
    }

    public User(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Lifecycle callbacks
    @PrePersist
    private void prePersist() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    // Helper methods for role management
    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(Role role) {
        return role != null && this.roles.contains(role);
    }

    public boolean hasAnyRole(Role... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        for (Role role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllRoles(Role... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        for (Role role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    // Business methods
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean isHotelUser() {
        return this.hotel != null;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public Set<Role> getRoles() {
        return new HashSet<>(roles); // Return defensive copy
    }

    public void setRoles(Set<Role> roles) {
        this.roles.clear();
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
        if (staff != null && staff.getUser() != this) {
            staff.setUser(this);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }



    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", lastLogin=" + lastLogin +
                ", createdAt=" + createdAt +
                '}';
    }
}