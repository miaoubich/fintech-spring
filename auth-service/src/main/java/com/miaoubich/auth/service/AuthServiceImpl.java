package com.miaoubich.auth.service;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miaoubich.auth.dto.AuthResponse;
import com.miaoubich.auth.dto.LoginRequest;
import com.miaoubich.auth.dto.RegisterRequest;
import com.miaoubich.auth.dto.UpdatePasswordRequest;
import com.miaoubich.auth.model.User;
import com.miaoubich.auth.model.UserRole;
import com.miaoubich.auth.repository.UserRepository;

@Service
public class AuthServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminService keycloakAdminService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakAdminService = keycloakAdminService;
    }

    /*
     *  REGISTER
     */

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Set<UserRole> roles = (request.roles() != null && !request.roles().isEmpty())
                ? request.roles()
                : Set.of(UserRole.USER);

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPendingKeycloakPassword(request.password()); // plain text, wiped after sync
        user.setRoles(roles);
        user.setSynced(false);

        userRepository.save(user);
        LOG.info("Registered user {} locally — pending Keycloak sync", user.getUsername());

        return new AuthResponse(
                null, null, null, null,
                user.getUsername(), user.getEmail(), user.getRoles(),
                false,
                "Registration successful. Account will sync with Keycloak shortly."
        );
    }

    /*
     *  LOGIN
     */

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // --- If not yet synced, try to sync NOW (on-demand) ---
        if (!user.isSynced()) {
            // Verify local password first
            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            try {
                String keycloakId = keycloakAdminService.syncUserToKeycloak(user);
                user.setKeycloakId(keycloakId);
                user.setSynced(true);
                user.setPendingKeycloakPassword(null); // wipe plain-text password
                userRepository.save(user);
                LOG.info("On-demand sync succeeded for user {}", user.getUsername());
            } catch (Exception e) {
                LOG.error("On-demand Keycloak sync failed for user {}: {}",
                        user.getUsername(), e.getMessage());
                throw new IllegalStateException(
                        "Account not yet synced with identity provider. Please try again later.");
            }
        }

        // Delegate to Keycloak password grant
        try {
            Map<String, Object> tokenResponse =
                    keycloakAdminService.loginWithKeycloak(request.username(), request.password());

            return new AuthResponse(
                    (String) tokenResponse.get("access_token"),
                    (String) tokenResponse.get("refresh_token"),
                    ((Number) tokenResponse.get("expires_in")).longValue(),
                    (String) tokenResponse.get("token_type"),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles(),
                    true,
                    "Login successful"
            );
        } catch (Exception e) {
            LOG.error("Keycloak login failed for user {}: {}", user.getUsername(), e.getMessage());
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    /*
     *  REFRESH
     */

    public AuthResponse refresh(String refreshToken) {
        Map<String, Object> tokenResponse = keycloakAdminService.refreshToken(refreshToken);

        return new AuthResponse(
                (String) tokenResponse.get("access_token"),
                (String) tokenResponse.get("refresh_token"),
                ((Number) tokenResponse.get("expires_in")).longValue(),
                (String) tokenResponse.get("token_type"),
                null, null, null,
                true,
                "Token refreshed"
        );
    }

    /*
     *  UPDATE PASSWORD
     */

    @Transactional
    public AuthResponse updatePassword(String username, UpdatePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password locally
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update local hash
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        // Update in Keycloak if synced
        if (user.isSynced() && user.getKeycloakId() != null) {
            keycloakAdminService.resetPasswordInKeycloak(user.getKeycloakId(), request.newPassword());
            LOG.info("Password updated in Keycloak for user {}", username);
        }

        userRepository.save(user);

        return new AuthResponse(
                null, null, null, null,
                user.getUsername(), user.getEmail(), user.getRoles(),
                user.isSynced(),
                "Password updated successfully"
        );
    }
}