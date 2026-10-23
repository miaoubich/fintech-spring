package com.miaoubich.auth.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.miaoubich.auth.model.User;
import com.miaoubich.auth.model.UserRole;

@Service
public class KeycloakAdminService {

	private static final Logger LOG = LoggerFactory.getLogger(KeycloakAdminService.class);

	private final RestClient restClient;

	@Value("${keycloak.server-url}")
	private String serverUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.client-id}")
	private String clientId;

	@Value("${keycloak.client-secret}")
	private String clientSecret;

	// Admin tokecn cache
	private String cachedAdminToken;
	private Instant adminTokenExpiry = Instant.MIN;

	public KeycloakAdminService(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	/*
	 * 1. Exchange username + password for a Keycloak JWT Used when the user is
	 * already synced
	 */
	public Map<String, Object> loginWithKeycloak(String username, String password) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "password");
		form.add("client_id", clientId);
		if (clientSecret != null && !clientSecret.isBlank()) {
			form.add("client_secret", clientSecret);
		}
		form.add("username", username);
		form.add("password", password);

		return restClient.post().uri(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
	}

	/*
	 * 2. Token refresh
	 */
	public Map<String, Object> refreshToken(String refreshToken) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "refresh_token");
		form.add("client_id", clientId);
		if (clientSecret != null && !clientSecret.isBlank()) {
			form.add("client_secret", clientSecret);
		}
		form.add("refresh_token", refreshToken);

		return restClient.post().uri(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
	}

	/*
	 * 3. Sync a local User entity to Keycloak (Create/Find User + Set Password +
	 * Assign Roles) Creates the user in Keycloak, assigns realm roles, and returns
	 * the Keycloak internal UUID.
	 *
	 * Steps: 1. POST /admin/realms/{realm}/users → create user with password 2.
	 * Extract UUID from Location header 3. For each UserRole → resolve/create
	 * Keycloak role → assign to user
	 * 
	 * @return Keycloak user UUID (e.g. "a1b2c3d4-...")
	 * 
	 * @throws HttpClientErrorException.Conflict if username already exists in
	 * Keycloak
	 */
	public String syncUserToKeycloak(User user) {
		String token = getAdminToken();

		// A. Create user in Keycloak
		Map<String, Object> keycloakUser = new LinkedHashMap<>();
		keycloakUser.put("username", user.getUsername());
		keycloakUser.put("email", user.getEmail());
		keycloakUser.put("firstName", user.getFirstName());
		keycloakUser.put("lastName", user.getLastName());
		keycloakUser.put("enabled", true);
		keycloakUser.put("emailVerified", true);

		// Set pwd in the same request (avoid a second API call)
		Map<String, Object> credential = new LinkedHashMap<>();
		credential.put("type", "password");
		credential.put("value", user.getPendingKeycloakPassword());
		credential.put("temporary", false);
		keycloakUser.put("credentials", List.of(credential));

		String keycloakId;
		try {
			ResponseEntity<Void> response = restClient
					.post()
					.uri(adminUsersEndpoint())
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.body(keycloakUser)
					.retrieve()
					.toBodilessEntity();

			// Keycloak returns 201 with Location: .../users/{uuid}
			String location = response.getHeaders().getFirst("location");
			if (location == null || location.isBlank()) {
				throw new IllegalStateException("Keycloak did not return a Location header");
			}
			keycloakId = location.substring(location.lastIndexOf('/') + 1);
			LOG.info("Created Keycloak user {} → UUID {}", user.getUsername(), keycloakId);
		} catch (HttpClientErrorException.Conflict e) {
			// User already exists in Keycloak — look up the existing UUID
			LOG.warn("User {} already exists in Keycloak, looking up UUID", user.getUsername());
			keycloakId = findKeycloakUserIdByUsername(user.getUsername(), token);
		}

		// B: Assign client roles ----
		assignClientRolesToUser(keycloakId, user.getRoles(), token);

		return keycloakId;
	}

	/*
	 * 4. Update a user's password in Keycloak.
	 */
	public void resetPasswordInKeycloak(String keycloakId, String newPassword) {
		String token = getAdminToken();

		Map<String, Object> credential = new LinkedHashMap<>();
		credential.put("type", "password");
		credential.put("value", newPassword);
		credential.put("temporary", false);

		restClient.put()
				.uri(adminUsersEndpoint() + "/" + keycloakId + "/reset-password")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.body(credential)
				.retrieve()
				.toBodilessEntity();
		
		LOG.info("Reset Keycloak password for user UUID {}", keycloakId);
	}
	
	/**
     * 5. Delete a user from Keycloak (optional — for user deactivation).
     */
    public void deleteUserFromKeycloak(String keycloakId) {
        String token = getAdminToken();

        restClient.delete()
                .uri(adminUsersEndpoint() + "/" + keycloakId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();

        LOG.info("Deleted Keycloak user UUID {}", keycloakId);
    }

    /**
     * Resolve the fintech client's internal UUID (uses view-clients role),
     * then assign the matching client roles to the user.
     */
	private void assignClientRolesToUser(String userUuid, Set<UserRole> roles, String token) {
		if (roles == null || roles.isEmpty()) 
			return;

        // 1. Find the client UUID for "fintech"
        String clientUuid = findClientUuid(token);

        // 2. Get all available roles for this client
        List<Map<String, Object>> availableRoles = restClient
        		.get()
                .uri(adminClientsEndpoint() + "/" + clientUuid + "/roles")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (availableRoles == null || availableRoles.isEmpty()) {
            LOG.warn("No client roles found for client {}", clientId);
            return;
        }

        // 3. Map UserRole enum → Keycloak role names, then filter
        Set<String> targetRoleNames = roles.stream()
								                .map(this::toKeycloakRoleName)
								                .collect(Collectors.toSet());

        List<Map<String, Object>> rolesToAssign = availableRoles.stream()
				                .filter(r -> targetRoleNames.contains(r.get("name")))
				                .collect(Collectors.toList());

        if (rolesToAssign.isEmpty()) {
            LOG.warn("None of the requested roles {} exist in Keycloak client {}", targetRoleNames, clientId);
            return;
        }
        
     // 4. Assign
        restClient
        		.post()
                .uri(adminUsersEndpoint() + "/" + userUuid + "/role-mappings/clients/" + clientUuid)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(rolesToAssign)
                .retrieve()
                .toBodilessEntity();

        LOG.info("Assigned roles {} to Keycloak user {}", targetRoleNames, userUuid);
	}
	
	/**
     * Map your UserRole enum to the Keycloak client role name.
     *
     *   You created "client_user" and "client_admin" in Keycloak.
     *     Adjust this mapping to match whatever roles you actually create.
     */
    private String toKeycloakRoleName(UserRole role) {
        return role.getKeycloakRoleName();
    }

	private String findClientUuid(String token) {
		List<Map<String, Object>> clients = restClient
						.get()
		                .uri(adminClientsEndpoint() + "?clientId={clientId}", clientId)
		                .header("Authorization", "Bearer " + token)
		                .retrieve()
		                .body(new ParameterizedTypeReference<>() {});

        if (clients == null || clients.isEmpty()) {
            throw new IllegalStateException("Client " + clientId + " not found in Keycloak");
        }
        return (String) clients.get(0).get("id");
	}

	/**
     * Look up an existing Keycloak user by username (uses view-users role).
     */
	private String findKeycloakUserIdByUsername(String username, String token) {
		List<Map<String, Object>> users = restClient
				.get()
                .uri(adminUsersEndpoint() + "?username={username}&exact=true", username)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (users == null || users.isEmpty()) {
            throw new IllegalStateException("User " + username + " not found in Keycloak");
        }
        return (String) users.get(0).get("id");
	}

	private String getAdminToken() {
		// Return cached token if still valid (with 30s safety margin)
        if (cachedAdminToken != null && Instant.now().isBefore(adminTokenExpiry.minusSeconds(30))) {
            return cachedAdminToken;
        }
        
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> response = restClient
        		.post()
                .uri(tokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        cachedAdminToken = (String) response.get("access_token");
        int expiresIn = (int) response.get("expires_in");
        adminTokenExpiry = Instant.now().plusSeconds(expiresIn);

        LOG.debug("Obtained new service-account token, expires in {}s", expiresIn);
        
        return cachedAdminToken;
	}

	private String tokenEndpoint() {
		return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
	}
	
	private String adminUsersEndpoint() {
		return serverUrl + "/admin/realms/" + realm + "/users";
	}
	
	 private String adminClientsEndpoint() {
	        return serverUrl + "/admin/realms/" + realm + "/clients";
	    }
}
