package com.yakrooms.be.dto.mapper;

import com.yakrooms.be.dto.response.UserResponse;
import com.yakrooms.be.model.entity.User;

public class UserMapper {

	public static UserResponse toUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setPhone(user.getPhone());
		response.setProfilePicUrl(user.getProfilePicUrl());
		response.setRole(user.getRole());
		response.setHotelId(user.getHotel() != null ? user.getHotel().getId() : null);

		return response;

	}
}