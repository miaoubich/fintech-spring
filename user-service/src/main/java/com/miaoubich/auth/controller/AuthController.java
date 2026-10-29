package com.miaoubich.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.auth.dto.AuthResponse;
import com.miaoubich.auth.dto.LoginRequest;
import com.miaoubich.auth.dto.RegisterRequest;
import com.miaoubich.auth.dto.UpdatePasswordRequest;
import com.miaoubich.auth.service.AuthServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PutMapping("/password")
    public ResponseEntity<AuthResponse> updatePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePasswordRequest request) {
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.ok(authService.updatePassword(username, request));
    }
}
