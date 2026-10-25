package com.super_spring.product_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.super_spring.product_service.config.ApplicationConstants;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                keycloakRoleConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ApplicationConstants.ACTUATOR_PATH).permitAll()
                .requestMatchers(HttpMethod.GET, ApplicationConstants.EXPORT_PATH).authenticated()
                .requestMatchers(HttpMethod.GET, ApplicationConstants.EXPORT_FILE_PATH).authenticated()
                .requestMatchers(HttpMethod.GET, ApplicationConstants.PRODUCTS_PATH,
                        ApplicationConstants.PRODUCTS_PATH + "/**").permitAll()
                .requestMatchers(ApplicationConstants.PRODUCTS_PATH + "/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }
}