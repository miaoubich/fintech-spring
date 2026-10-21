package com.miaoubich.auth.dto;

import java.util.Set;

import com.miaoubich.auth.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

		@NotBlank String username, 
		@NotBlank @Email String email, 
		@NotBlank String firstName, 
		@NotBlank String lastName,
		@NotBlank @Size(min = 6, message = "Password must be at least 6 characters") 
		String password,
		Set<UserRole> roles) {

}
