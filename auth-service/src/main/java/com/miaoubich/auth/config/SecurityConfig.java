package com.miaoubich.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }
    
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
					// Public auth endpoints
					.requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh"
				).permitAll()
				// Actuator health
				.requestMatchers("/actuator/**").permitAll()	
				.requestMatchers("/super/**").hasRole("super")
			    .requestMatchers("/admin/**").hasAnyRole("admin", "admin1")
			    .requestMatchers("/audit/**").hasRole("audit")
			    .requestMatchers("/fraud/**").hasAnyRole("fraud", "fraud1")
			    .requestMatchers("/compliance/**").hasAnyRole("compliance", "compliance1", "compliance2")
			    .requestMatchers("/support/**").hasAnyRole("support", "support1", "support2")
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth2 -> 
									oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
	
		return http.build();
	}
	
	/**
     * BCrypt encoder for local password hashing.
     * Used during the async-sync window (before Keycloak takes over).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
