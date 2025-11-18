package com.smart.edilek.security.jwt;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class KeycloakJwtUtils {

    /**
     * Extract username from Keycloak JWT token
     */
    public String getUsernameFromJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            // Keycloak typically uses 'preferred_username' or 'sub' for username
            String username = jwt.getClaimAsString("preferred_username");
            return username != null ? username : jwt.getSubject();
        }
        return null;
    }

    /**
     * Extract user email from Keycloak JWT token
     */
    public String getEmailFromJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaimAsString("email");
        }
        return null;
    }

    /**
     * Extract user roles from Keycloak JWT token
     */
    public Collection<String> getRolesFromJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Extract realm roles from Keycloak JWT token
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getRealmRolesFromJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.get("roles") != null) {
                return (Collection<String>) realmAccess.get("roles");
            }
        }
        return null;
    }

    /**
     * Extract client roles from Keycloak JWT token
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getClientRolesFromJwtToken(Authentication authentication, String clientId) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null && resourceAccess.get(clientId) != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                if (clientAccess.get("roles") != null) {
                    return (Collection<String>) clientAccess.get("roles");
                }
            }
        }
        return null;
    }

    /**
     * Extract user ID from Keycloak JWT token
     */
    public String getUserIdFromJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getSubject(); // 'sub' claim contains user ID
        }
        return null;
    }

    /**
     * Check if JWT token is valid (not expired)
     */
    public boolean validateJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getExpiresAt() != null && 
                   jwt.getExpiresAt().isAfter(java.time.Instant.now());
        }
        return false;
    }

    /**
     * Extract all custom claims from JWT token
     */
    public Map<String, Object> getAllClaimsFromJwtToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getClaims();
        }
        return null;
    }
}