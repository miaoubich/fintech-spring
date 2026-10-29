package com.miaoubich.auth.dto;

import java.util.Set;

import com.miaoubich.auth.model.UserRole;

public record AuthResponse(
		
		String accessToken,
	    String refreshToken,
	    Long expiresIn,
	    String tokenType,
	    String username,
	    String email,
	    Set<UserRole> roles,
	    boolean syncedWithKeycloak,
	    String message
		) {

}
