package com.miaoubich.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Set<String> roles
) {}