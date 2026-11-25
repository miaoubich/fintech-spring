package com.miaoubich.agent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.miaoubich.agent.dto.PageResponse;
import com.miaoubich.agent.dto.UserResponse;

@Component
public class UserClient {

    private static final Logger LOG = LoggerFactory.getLogger(UserClient.class);
    private final RestClient restClient;

    public UserClient(RestClient.Builder restClient) {
        this.restClient = restClient
                .baseUrl("http://USER-SERVICE:8085")
                .build();
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/all")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<UserResponse>>() {});
        } catch (Exception e) {
            LOG.error("Failed to fetch users from user-service: {}", e.getMessage());
            return null;
        }
    }
}