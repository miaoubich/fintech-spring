package com.miaoubich.auth.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities =
                Stream.concat(
                        extractRealmRoles(jwt),
                        extractClientRoles(jwt)
                ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }

    @SuppressWarnings("unchecked")
    private Stream<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) 
        	return Stream.empty();

        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) 
        	return Stream.empty();

        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r));
    }

    @SuppressWarnings("unchecked")
    private Stream<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null) 
        	return Stream.empty();

        Map<String, Object> client = (Map<String, Object>) resourceAccess.get(clientId);
        if (client == null) 
        	return Stream.empty();

        List<String> roles = (List<String>) client.get("roles");
        if (roles == null) 
        	return Stream.empty();

        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r));
    }
}