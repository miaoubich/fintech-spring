package com.miaoubich.customerservice.dto;

import java.util.List;

import com.miaoubich.customerservice.model.CustomerStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CustomerDto(
		Long id,
	    @NotBlank String firstName,
	    @NotBlank String lastName,
	    @NotBlank @Email String email,
	    CustomerStatus status,
	    String supportNotes,
	    @NotEmpty(message = "Customer must have at least 1 address")
	    @Valid List<AddressDto> addresses
		) {

}
