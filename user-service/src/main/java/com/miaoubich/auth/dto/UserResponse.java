package com.miaoubich.auth.dto;

import java.util.Set;

import com.miaoubich.auth.model.UserRole;

public record UserResponse(
		
		String username,
		String email,
		String firstName,
		String lastName,
		boolean enabled,
		Set<UserRole> roles

		) {

	}
