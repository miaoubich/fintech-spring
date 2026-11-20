package com.miaoubich.auth.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miaoubich.auth.model.User;
import com.miaoubich.auth.repository.UserRepository;
import com.miaoubich.auth.service.UserServiceImpl;

@Component
public class KeycloakSyncScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakSyncScheduler.class);

    private final UserRepository userRepository;
    private final UserServiceImpl userServiceImpl;

    public KeycloakSyncScheduler(UserRepository userRepository,
    							UserServiceImpl userServiceImpl) {
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
    }

    @Scheduled(fixedRate = 60_000, // 1 minute
    		initialDelay = 60_000)
    public void syncUnsyncedUsers() {
        LOG.info("Checking for unsynced users...");

        List<User> unsynced = userRepository.findAll().stream()
                .filter(u -> !u.isSynced())
                .toList();

        if (unsynced.isEmpty()) {
            LOG.info("No unsynced users found.");
            return;
        }

        LOG.info("Found {} unsynced user(s) — starting Keycloak sync", unsynced.size());

        for (User user : unsynced) {
        	userServiceImpl.syncSingleUser(user); // each user in its own transaction
        }
    }
}