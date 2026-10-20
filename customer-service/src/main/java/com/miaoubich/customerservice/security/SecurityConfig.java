package com.miaoubich.customerservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.miaoubich.customerservice.security.SecurityConstants.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()

                // 1. Specialty / Admin routes first (Most specific)
                .requestMatchers("/customers/admin/**").hasAuthority(CUSTOMER_FULL)
                .requestMatchers("/customers/audit/**").hasAuthority(CUSTOMER_AUDIT)
                .requestMatchers(HttpMethod.PATCH, "/customers/*/suspend").hasAuthority(CUSTOMER_SUSPEND)
                .requestMatchers(HttpMethod.PATCH, "/customers/*/support").hasAuthority(CUSTOMER_SUPPORT)

                // 2. Write / Delete operations
                .requestMatchers(HttpMethod.POST, "/customers/**").hasAuthority(CUSTOMER_WRITE)
                .requestMatchers(HttpMethod.PUT, "/customers/**").hasAuthority(CUSTOMER_WRITE)
                .requestMatchers(HttpMethod.DELETE, "/customers/**").hasAuthority(CUSTOMER_DELETE)

                // 3. Read-only operations (user1, user2, user3, and all higher roles)
                .requestMatchers(HttpMethod.GET, "/customers/**").hasAuthority(CUSTOMER_READ)

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> rawRoles = new HashSet<>();

        // 1. Extract Realm Roles (realm_access.roles)
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess != null && realmAccess.get(ROLES_CLAIM) instanceof Collection<?> roles) {
            roles.forEach(r -> rawRoles.add(r.toString()));
        }

        // 2. Extract Client Roles (resource_access.fintech.roles)
        Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess != null && resourceAccess.get(FINTECH_CLIENT_ID) instanceof Map<?, ?> fintechClient) {
            if (fintechClient.get(ROLES_CLAIM) instanceof Collection<?> clientRoles) {
                clientRoles.forEach(r -> rawRoles.add(r.toString()));
            }
        }

        // 3. Map the user's JWT roles to Spring Security GrantedAuthorities
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (String role : rawRoles) {
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role));

            ROLE_TO_AUTHORITIES.getOrDefault(role, Set.of())
                    .forEach(auth -> authorities.add(new SimpleGrantedAuthority(auth)));
        }

        return authorities;
    }
}