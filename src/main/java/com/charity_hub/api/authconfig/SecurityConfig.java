package com.charity_hub.api.authconfig;

import com.charity_hub.domain.contracts.ILogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final ILogger logger;

    public SecurityConfig(ILogger logger) {
        this.logger = logger;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtVerifier jwtVerifier) throws Exception {
        http.csrf(csrf -> csrf.disable());

        // Set session management to stateless (for JWT authentication)
        http.sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> {
            try {
                auth
                    .requestMatchers(new AntPathRequestMatcher("/actuator")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/v1/accounts/authenticate")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                    .anyRequest().authenticated();
            } catch (Exception e) {
                throw new RuntimeException("Error configuring security rules", e);
            }
        });

        // Configure async security
        http.securityContext(securityContext ->
            securityContext.requireExplicitSave(false)
        );

        http.addFilterBefore(
            new JwtAuthFilter(jwtVerifier, new ObjectMapper(), logger),
            AnonymousAuthenticationFilter.class
        );

        return http.build();
    }
}