package com.miaoubich.auth.commonmethods;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class CommonMethods {

	private static final Logger LOG = LoggerFactory.getLogger(CommonMethods.class);

	private final RestClient restClient;

	@Value("${keycloak.server-url}")
	private String serverUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.client-id}")
	private String clientId;

	@Value("${keycloak.client-secret}")
	private String clientSecret;

	// Admin token cache
	private String cachedAdminToken;
	private Instant adminTokenExpiry = Instant.MIN;

	public CommonMethods(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	public String getAdminToken() {
		if (cachedAdminToken != null && Instant.now().isBefore(adminTokenExpiry.minusSeconds(30))) {
			return cachedAdminToken;
		}

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "client_credentials");
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);

		Map<String, Object> response = restClient.post().uri(tokenEndpoint())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

		cachedAdminToken = (String) response.get("access_token");
		int expiresIn = (int) response.get("expires_in");
		adminTokenExpiry = Instant.now().plusSeconds(expiresIn);

		LOG.debug("Obtained new service-account token, expires in {}s", expiresIn);

		return cachedAdminToken;
	}
	
	public String tokenEndpoint() {
		return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
	}
	
	public String adminUsersEndpoint() {
		return serverUrl + "/admin/realms/" + realm + "/users";
	}

	public String adminClientsEndpoint() {
		return serverUrl + "/admin/realms/" + realm + "/clients";
	}

	public String adminRealmRolesEndpoint() {
		return serverUrl + "/admin/realms/" + realm + "/roles";
	}
}
