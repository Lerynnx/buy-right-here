package com.buy_right_here.inventory_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.buy_right_here.inventory_service.config.ApplicationConstants;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;

    public SecurityConfig(KeycloakRoleConverter keycloakRoleConverter) {
        this.keycloakRoleConverter = keycloakRoleConverter;
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(ApplicationConstants.ACTUATOR_PATH).permitAll()
                        .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, ApplicationConstants.INVENTORY_PATH,
                                ApplicationConstants.INVENTORY_PATH + "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, ApplicationConstants.INVENTORY_PATH,
                                ApplicationConstants.INVENTORY_PATH + "/**")
                        .hasAnyRole(ApplicationConstants.ADMIN_ROLE, ApplicationConstants.MANAGER_ROLE)
                        .pathMatchers(HttpMethod.PUT, ApplicationConstants.INVENTORY_PATH + "/**")
                        .hasAnyRole(ApplicationConstants.ADMIN_ROLE, ApplicationConstants.MANAGER_ROLE)
                        .pathMatchers(HttpMethod.DELETE, ApplicationConstants.INVENTORY_PATH + "/**")
                        .hasAnyRole(ApplicationConstants.ADMIN_ROLE, ApplicationConstants.MANAGER_ROLE)
                        .pathMatchers(ApplicationConstants.INVENTORY_PATH + "/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }
}
