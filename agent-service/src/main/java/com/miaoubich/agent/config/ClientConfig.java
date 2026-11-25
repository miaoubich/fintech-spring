package com.miaoubich.agent.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    Authentication auth = SecurityContextHolder
                    		.getContext()
                    		.getAuthentication();
                    
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + 
                        									jwtAuth.getToken().getTokenValue());
                    }
                    return execution.execute(request, body);
                });
    }
}