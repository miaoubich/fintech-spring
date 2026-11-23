package com.miaoubich.agent.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new HashSet<>(jwtGrantedAuthoritiesConverter.convert(jwt));

        // Extract Keycloak client roles from "resource_access.fintech.roles"
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Object fintech = resourceAccess.get("fintech");
            if (fintech instanceof Map) {
                List<String> roles = (List<String>) ((Map<String, Object>) fintech).get("roles");
                if (roles != null) {
                    roles.forEach(r -> authorities.add(() -> "ROLE_" + r));
                }
            }
        }

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                roles.forEach(r -> authorities.add(() -> "ROLE_" + r));
            }
        }

        return authorities;
    }
}