package com.yakrooms.be.dto.response;

import com.google.auto.value.AutoValue.Builder;
import com.yakrooms.be.model.enums.Role;

import java.util.List;


@Builder
public class UserResponse {
	private Long id;
	private String name;
	private String email;
	private String phone;
	private String profilePicUrl;
	private List<Role> roles;
	private Long hotelId;
	private boolean detailSet;

	public UserResponse() {
		super();
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}

	public boolean isDetailSet() {
		return detailSet;
	}

	public void setDetailSet(boolean detailSet) {
		this.detailSet = detailSet;
	}

}