package com.miaoubich.auth.controller;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miaoubich.auth.dto.ReconciliationResult;
import com.miaoubich.auth.dto.UserResponse;
import com.miaoubich.auth.service.UserServiceImpl;

@RestController
@RequestMapping("/user")
public class UserController {
	
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserController.class);

	private final UserServiceImpl userService;

	public UserController(UserServiceImpl userService) {
		this.userService = userService;
	}
	
	@PreAuthorize("hasAnyRole('super', 'admin', 'admin1')")
	@GetMapping("/all")
	public ResponseEntity<Page<UserResponse>> getAllUsers(
					@AuthenticationPrincipal Jwt jwt,
					@PageableDefault(size = 10, sort = "username") Pageable pageable) {
		
		String actorUsername = jwt.getClaimAsString("preferred_username");
		
		LOG.warn("AUDIT: User [{}] accessed user directory listing. Page: {}, Size: {}", 
	             actorUsername, pageable.getPageNumber(), pageable.getPageSize());
		
		return ResponseEntity.ok(userService.getAllUsers(pageable));
	}
	
	/**
     * Reconciliation endpoint — ONLY super users can run this.
     */
    @PreAuthorize("hasRole('super')")
    @PostMapping("/reconcile")
    public ResponseEntity<ReconciliationResult> reconcileUsers() {
        LOG.warn("AUDIT: Reconciliation triggered between PostgreSQL and Keycloak");
        ReconciliationResult result = userService.reconcileUsers();
        return ResponseEntity.ok(result);
    }
}
