package com.flagforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize checks like @rbac.hasRole(...)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // cost factor 12 — see docs/05-security.md
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // stateless JWT auth; CSRF only relevant to the cookie-based refresh flow (see security doc)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/oauth2/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/evaluate", "/api/v1/stream").authenticated() // API-key auth, see ApiKeyAuthFilter (not shown in scaffold)
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .oauth2Login(oauth2 -> {}); // Google OAuth2 login flow

        return http.build();
    }

    // NOTE: In the full implementation, JwtDecoder is configured with the app's own
    // RS256 key pair (not an external issuer), since FlagForge issues its own tokens.
    // Omitted here for scaffold brevity — see docs/05-security.md for the design.
}
