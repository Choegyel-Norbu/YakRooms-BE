package com.yakrooms.be.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "staff",
    indexes = {
        @Index(name = "idx_staff_email", columnList = "email", unique = true),
        @Index(name = "idx_staff_hotel_id", columnList = "hotel_id"),
        @Index(name = "idx_staff_user_id", columnList = "user_id"),
        @Index(name = "idx_staff_hotel_position", columnList = "hotel_id, position"),
        @Index(name = "idx_staff_date_joined", columnList = "date_joined"),
        @Index(name = "idx_staff_hotel_date_joined", columnList = "hotel_id, date_joined")
    }
)
public class Staff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(name = "phone_number", nullable = false, length = 20)
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @Column(length = 50)
    private String position;
    
    @Column(name = "date_joined")
    private LocalDate dateJoined;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;
    
    @PrePersist
    protected void onCreate() {
        if (dateJoined == null) {
            dateJoined = LocalDate.now();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff staff = (Staff) o;
        return Objects.equals(email, staff.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

	public Staff() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public LocalDate getDateJoined() {
		return dateJoined;
	}

	public void setDateJoined(LocalDate dateJoined) {
		this.dateJoined = dateJoined;
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
    
    
}