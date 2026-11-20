package com.miaoubich.auth.dto;

import java.util.List;

public record ReconciliationResult(
        int createdInKeycloak,
        int disabledInKeycloak,
        List<String> errors
) {}