package com.miaoubich.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private static final String REALM_ROLES_CLAIM = "realm_access.roles";
    private static final String FINTECH_ROLES_CLAIM = "resource_access.fintech.roles";

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                .pathMatchers("/api/auth/register", "/api/auth/login", "/actuator/**").permitAll()

                .pathMatchers("/customers/**").hasAuthority("CUSTOMER_READ")
                .pathMatchers(HttpMethod.POST, "/customers/**").hasAuthority("CUSTOMER_WRITE")
                .pathMatchers(HttpMethod.PUT, "/customers/**").hasAuthority("CUSTOMER_WRITE")
                .pathMatchers(HttpMethod.DELETE, "/customers/**").hasAuthority("CUSTOMER_DELETE")
                .pathMatchers("/customers/admin/**").hasAuthority("CUSTOMER_FULL")

                .pathMatchers("/trades/**").hasAuthority("TRADE_READ")
                .pathMatchers("/ledger/**").hasAuthority("LEDGER_READ")
                .pathMatchers("/wallet/**").hasAuthority("WALLET_READ")

                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(reactiveJwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        // In WebFlux the authorities converter must return Converter<Jwt, Flux<GrantedAuthority>>
        converter.setJwtGrantedAuthoritiesConverter(jwt 
        									-> Flux.fromIterable(extractAuthorities(jwt)));
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> rawRoles = new HashSet<>();
        rawRoles.addAll(extractRoles(jwt.getClaim(REALM_ROLES_CLAIM)));
        rawRoles.addAll(extractRoles(jwt.getClaim(FINTECH_ROLES_CLAIM)));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // keep raw roles as ROLE_* authorities (optional, useful for hasRole)
        for (String role : rawRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // map Keycloak roles to functional business authorities
        if (rawRoles.contains("super")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_FULL"));
        }
        if (rawRoles.contains("admin") || rawRoles.contains("admin1")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_WRITE"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_DELETE"));
        }
        if (Set.of("user1", "user2", "user3").contains(rawRoles)) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
        }
        if (rawRoles.contains("compliance1") || rawRoles.contains("compliance2")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_AUDIT"));
        }
        if (rawRoles.contains("fraud1")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_SUSPEND"));
        }
        if (rawRoles.contains("support1") || rawRoles.contains("support2")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_SUPPORT"));
        }

        return authorities;
    }

    private List<String> extractRoles(Object claim) {
        if (claim == null) return List.of();
        if (claim instanceof String s) return List.of(s);
        if (claim instanceof List<?> list) return list.stream().map(Object::toString).toList();

        // fallback for org.json.JSONArray if your JWT parser ever uses it
        if (claim instanceof org.json.JSONArray arr) {
            List<Object> items = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) items.add(arr.get(i));
            return items.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}