package com.buy_right_here.inventory_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test
    void convertsRealmRolesToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", java.util.Map.of("roles", List.of("ADMIN", "MANAGER")))
                .build();

        assertEquals(
                List.of("ROLE_ADMIN", "ROLE_MANAGER"),
                converter.convert(jwt).stream().map(authority -> authority.getAuthority()).toList());
    }

    @Test
    void returnsNoAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        assertEquals(List.of(), converter.convert(jwt));
    }
}
