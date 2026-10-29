package com.miaoubich.config;

import java.util.*;
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

    private static final String CLIENT_ID = "fintech";

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                // Match the actual route predicate (/auth/**)
                .pathMatchers("/auth/**", "/actuator/**").permitAll()

                // Customer routes
                .pathMatchers(HttpMethod.GET, "/customers/**").hasAuthority("CUSTOMER_READ")
                .pathMatchers(HttpMethod.POST, "/customers/**").hasAuthority("CUSTOMER_WRITE")
                .pathMatchers(HttpMethod.PUT, "/customers/**").hasAuthority("CUSTOMER_WRITE")
                .pathMatchers(HttpMethod.DELETE, "/customers/**").hasAuthority("CUSTOMER_DELETE")
                .pathMatchers("/customers/admin/**").hasAuthority("CUSTOMER_FULL")

                // Domain microservice routes (Now mapped below!)
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
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Flux.fromIterable(extractAuthorities(jwt)));
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> rawRoles = new HashSet<>();
        rawRoles.addAll(extractRealmRoles(jwt));
        rawRoles.addAll(extractClientRoles(jwt, CLIENT_ID));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Keep raw roles prefixed with ROLE_ (e.g. ROLE_admin, ROLE_super)
        for (String role : rawRoles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // 2. Map Keycloak roles to domain business authorities

        // SUPER & ADMIN roles get full access across all domains
        if (rawRoles.contains("super") || rawRoles.contains("admin") || rawRoles.contains("admin1")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_FULL"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_WRITE"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_DELETE"));
            authorities.add(new SimpleGrantedAuthority("TRADE_READ"));
            authorities.add(new SimpleGrantedAuthority("LEDGER_READ"));
            authorities.add(new SimpleGrantedAuthority("WALLET_READ"));
        }

        // REGULAR USERS get read access to Trades, Wallet, Ledger, and Customer
        if (rawRoles.contains("user") || rawRoles.contains("user1") || rawRoles.contains("user2") || rawRoles.contains("user3")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("TRADE_READ"));
            authorities.add(new SimpleGrantedAuthority("LEDGER_READ"));
            authorities.add(new SimpleGrantedAuthority("WALLET_READ"));
        }

        // COMPLIANCE
        if (rawRoles.contains("compliance") || rawRoles.contains("compliance1") || rawRoles.contains("compliance2")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_AUDIT"));
            authorities.add(new SimpleGrantedAuthority("TRADE_READ"));
            authorities.add(new SimpleGrantedAuthority("LEDGER_READ"));
        }

        // FRAUD
        if (rawRoles.contains("fraud") || rawRoles.contains("fraud1")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_SUSPEND"));
            authorities.add(new SimpleGrantedAuthority("TRADE_READ"));
        }

        // SUPPORT
        if (rawRoles.contains("support") || rawRoles.contains("support1") || rawRoles.contains("support2")) {
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_READ"));
            authorities.add(new SimpleGrantedAuthority("CUSTOMER_SUPPORT"));
            authorities.add(new SimpleGrantedAuthority("WALLET_READ"));
        }

        return authorities;
    }

    /**
     * Safely extract realm roles from nested JWT map: realm_access -> roles
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            return roles.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    /**
     * Safely extract client roles from nested JWT map: resource_access -> fintech -> roles
     */
    @SuppressWarnings("unchecked")
    private List<String> extractClientRoles(Jwt jwt, String clientId) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null && resourceAccess.get(clientId) instanceof Map<?, ?> clientMap) {
            if (clientMap.get("roles") instanceof List<?> roles) {
                return roles.stream().map(Object::toString).toList();
            }
        }
        return List.of();
    }
}