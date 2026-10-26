package com.miaoubich.auth.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.miaoubich.auth.model.User;
import com.miaoubich.auth.repository.UserRepository;
import com.miaoubich.auth.service.KeycloakAdminService;

@Component
public class KeycloakSyncScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakSyncScheduler.class);

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    public KeycloakSyncScheduler(UserRepository userRepository,
                                 KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Scheduled(fixedRate = 60_000) // every 1 minute
    @Transactional
    public void syncUnsyncedUsers() {
        var unsynced = userRepository.findBySyncedFalse();
        if (unsynced.isEmpty()) 
        	return;

        LOG.info("Found {} unsynced user(s) — starting Keycloak sync", unsynced.size());

        for (var user : unsynced) {
            try {
                var keycloakId = keycloakAdminService.syncUserToKeycloak(user);
                user.setKeycloakId(keycloakId);
                user.setSynced(true);
                user.setEnabled(true);      
                user.setEmailVerified(true);
                user.setPendingKeycloakPassword(null); // wipe plain-text
                user.setLastSyncedAt(Instant.now());
                userRepository.save(user);
                LOG.info("Synced user {} → Keycloak UUID {}", user.getUsername(), keycloakId);
            } catch (Exception e) {
                LOG.error("Failed to sync user {}: {}", user.getUsername(), e.getMessage());
                // No re-throw — continue with next user
            }
        }
    }
}