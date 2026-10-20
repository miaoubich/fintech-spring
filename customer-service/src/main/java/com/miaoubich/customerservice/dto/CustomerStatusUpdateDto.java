package com.miaoubich.customerservice.dto;

import com.miaoubich.customerservice.model.CustomerStatus;

import jakarta.validation.constraints.NotNull;

public record CustomerStatusUpdateDto(
		
		@NotNull CustomerStatus status,
	    String supportNotes
		) {
}
