package com.miaoubich.auth.config;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
			// 1. Enable CORS using the bean below (CRITICAL FOR REACT)
        	.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
					// Explicitly allow all OPTIONS preflight requests
	                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	             // Public auth endpoints
	                .requestMatchers("/auth/**").permitAll()
	                // Public health & info
	                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
	                // Restricted Actuator endpoints
	                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("super")
	                // Role-based route restrictions
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
	
	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from Vite React dev server
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
