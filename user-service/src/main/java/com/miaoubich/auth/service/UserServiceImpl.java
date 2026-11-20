package com.miaoubich.auth.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.miaoubich.auth.commonmethods.CommonMethods;
import com.miaoubich.auth.dto.ReconciliationResult;
import com.miaoubich.auth.dto.UserResponse;
import com.miaoubich.auth.model.User;
import com.miaoubich.auth.repository.UserRepository;

@Service
public class UserServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    public UserServiceImpl(UserRepository userRepository,
                           KeycloakAdminService keycloakAdminService,
                           CommonMethods commonMethods,
                           RestClient.Builder restClientBuilder) {
        this.userRepository = userRepository;
        this.keycloakAdminService = keycloakAdminService;
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
		return userRepository.findAll(pageable)
				.map(user -> new UserResponse(
						user.getUsername(),
						user.getEmail(),
						user.getFirstName(),
						user.getLastName(),
						user.isEnabled(),
						user.getRoles()
				));
	}
    
    /*
     * Reconcile PostgreSQL <-> Keycloak users.
     * - DB user missing in Keycloak  -> create in Keycloak
     * - Keycloak user missing in DB  -> disable in Keycloak
     */
    @Transactional(readOnly = true)
    public ReconciliationResult reconcileUsers() {
        String token = keycloakAdminService.getAdminToken();

        // 1. Fetch from Keycloak
        List<Map<String, Object>> keycloakUsers = keycloakAdminService.getAllUsersFromKeycloak(token);
        Set<String> keycloakUsernames = keycloakUsers.stream()
                .map(u -> (String) u.get("username"))
                .collect(Collectors.toSet());

        // 2. Fetch from PostgreSQL
        List<User> dbUsers = userRepository.findAll();
        Set<String> dbUsernames = dbUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        int createdInKeycloak = 0;
        int disabledInKeycloak = 0;
        List<String> errors = new ArrayList<>();

        // --- Case A: User exists in PostgreSQL but missing in Keycloak ---
        for (User user : dbUsers) {
            if (!keycloakUsernames.contains(user.getUsername())) {
                try {
                	user.setEmailVerified(true);
                	user.setEnabled(true);
                	user.setLastSyncedAt(Instant.now());
                	user.setPendingKeycloakPassword(null);
                	user.setSynced(false);
                	
                    syncSingleUser(user);
                    
                    createdInKeycloak++;
                    LOG.info("Reconciliation: Created missing Keycloak user {}", user.getUsername());
                } catch (Exception e) {
                    String msg = "Failed to create Keycloak user " + user.getUsername() + ": " + e.getMessage();
                    LOG.error(msg);
                    errors.add(msg);
                }
            }
        }

        // --- Case B: User exists in Keycloak but missing in PostgreSQL ---
        for (Map<String, Object> kcUser : keycloakUsers) {
            String username = (String) kcUser.get("username");
            if (!dbUsernames.contains(username)) {
                try {
                    String kcId = (String) kcUser.get("id");
                    Boolean kcEnabled = (Boolean) kcUser.get("enabled");

                    // Skip if already disabled — don't increment counter
                    if (kcEnabled != null && !kcEnabled) {
                        LOG.info("Reconciliation: Orphaned Keycloak user {} already disabled, skipping.", username);
                        continue;
                    }
                    
                    keycloakAdminService.disableKeycloakUser(kcId, token);
                    disabledInKeycloak++;
                    LOG.info("Reconciliation: Disabled orphaned Keycloak user {}", username);
                } catch (Exception e) {
                    String msg = "Failed to disable Keycloak user " + username + ": " + e.getMessage();
                    LOG.error(msg);
                    errors.add(msg);
                }
            }
        }

        return new ReconciliationResult(createdInKeycloak, disabledInKeycloak, errors);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncSingleUser(User user) {
        User managed = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getUsername()));

        try {
            String token = keycloakAdminService.getAdminToken();

            String keycloakId;

            // 1. Check if user already exists in Keycloak (handles previous failed runs)
            try {
                keycloakId = keycloakAdminService.findKeycloakUserIdByUsername(managed.getUsername(), token);
                LOG.warn("User {} already exists in Keycloak (UUID {}). Updating PostgreSQL only.",
                        managed.getUsername(), keycloakId);
            } catch (IllegalStateException e) {
                // 2. Not in Keycloak → create it
                if (managed.getPendingKeycloakPassword() == null 
                        || managed.getPendingKeycloakPassword().isBlank()) {
                    managed.setPendingKeycloakPassword(
                        UUID.randomUUID().toString() + "Temp@1"
                    );
                }

                keycloakId = keycloakAdminService.syncUserToKeycloak(managed);
                LOG.info("Created Keycloak user {} → UUID {}", managed.getUsername(), keycloakId);
            }

            // 3. Update PostgreSQL to reflect Keycloak state
            managed.setEnabled(true);
            managed.setEmailVerified(true);
            managed.setKeycloakId(keycloakId);
            managed.setPendingKeycloakPassword(null); // wipe plain text
            managed.setLastSyncedAt(Instant.now());
            managed.setSynced(true);

            userRepository.save(managed);

            LOG.info("Synced user {} → Keycloak UUID {}; DB updated", 
                    managed.getUsername(), keycloakId);

        } catch (Exception e) {
            LOG.error("Failed to sync user {}: {}", managed.getUsername(), e.getMessage(), e);
            throw e; // REQUIRES_NEW rolls back only this user
        }
    }
}
