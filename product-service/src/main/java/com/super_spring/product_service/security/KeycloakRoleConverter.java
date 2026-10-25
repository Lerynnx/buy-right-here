package com.super_spring.product_service.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import com.super_spring.product_service.config.ApplicationConstants;

@Component
public class KeycloakRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Map<String, Object> realmAccess =
                jwt.getClaim(ApplicationConstants.KEYCLOAK_REALM_ACCESS_CLAIM);

        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesClaim = realmAccess.get(ApplicationConstants.KEYCLOAK_ROLES_CLAIM);
        if (!(rolesClaim instanceof List<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority(ApplicationConstants.ROLE_PREFIX + role))
                .map(authority -> (GrantedAuthority) authority)
                .toList();
    }
}