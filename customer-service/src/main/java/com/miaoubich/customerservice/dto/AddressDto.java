package com.miaoubich.customerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
		Long id,
	    @NotBlank String street,
	    @NotBlank String city,
	    @NotBlank String state,
	    @NotBlank String zipCode,
	    @NotBlank String country
		) {

}
