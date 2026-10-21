package com.miaoubich.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
		
		@NotBlank String currentPassword,
	    @NotBlank @Size(min = 6, message = "New password must be at least 6 characters") 
		String newPassword
		) {

}
