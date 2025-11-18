package com.smart.edilek.service.keycloak;

import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class KeycloakAdminService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminService.class);

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private Keycloak keycloak;

    @PostConstruct
    public void initKeycloak() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Admin realm is usually 'master'
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();

        logger.info("Keycloak admin client initialized for server: {}", serverUrl);
    }

    /**
     * Create a new user in Keycloak
     */
    public String createUser(String username, String email, String firstName, String lastName, String password) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create user
            var response = usersResource.create(user);
            String userId = extractUserIdFromLocation(response.getLocation());

            // Set password
            if (password != null && !password.isEmpty()) {
                setUserPassword(userId, password, false);
            }

            logger.info("User created successfully with ID: {}", userId);
            return userId;

        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Set user password
     */
    public void setUserPassword(String userId, String password, boolean temporary) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(temporary);

            userResource.resetPassword(credential);
            logger.info("Password set successfully for user: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to set password for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to set password", e);
        }
    }

    /**
     * Assign role to user
     */
    public void assignRoleToUser(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            userResource.roles().realmLevel().add(List.of(role));
            
            logger.info("Role '{}' assigned to user: {}", roleName, userId);

        } catch (Exception e) {
            logger.error("Failed to assign role '{}' to user {}: {}", roleName, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to assign role", e);
        }
    }

    /**
     * Remove role from user
     */
    public void removeRoleFromUser(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            userResource.roles().realmLevel().remove(List.of(role));
            
            logger.info("Role '{}' removed from user: {}", roleName, userId);

        } catch (Exception e) {
            logger.error("Failed to remove role '{}' from user {}: {}", roleName, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove role", e);
        }
    }

    /**
     * Get user by username
     */
    public UserRepresentation getUserByUsername(String username) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(username, true);
            
            if (!users.isEmpty()) {
                return users.get(0);
            }
            return null;

        } catch (Exception e) {
            logger.error("Failed to get user by username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to get user", e);
        }
    }

    /**
     * Update user attributes
     */
    public void updateUserAttributes(String userId, Map<String, List<String>> attributes) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);
            
            UserRepresentation user = userResource.toRepresentation();
            user.setAttributes(attributes);
            userResource.update(user);
            
            logger.info("User attributes updated for user: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to update user attributes for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user attributes", e);
        }
    }

    /**
     * Delete user
     */
    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.users().delete(userId);
            
            logger.info("User deleted successfully: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to delete user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Get all users
     */
    public List<UserRepresentation> getAllUsers(int first, int max) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            return realmResource.users().list(first, max);

        } catch (Exception e) {
            logger.error("Failed to get all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get users", e);
        }
    }

    private String extractUserIdFromLocation(java.net.URI location) {
        if (location == null) {
            throw new RuntimeException("Failed to extract user ID from location");
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}