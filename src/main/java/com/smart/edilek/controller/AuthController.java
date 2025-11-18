package com.smart.edilek.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.service.keycloak.KeycloakAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth Controller", description = "Keycloak authentication and authorization endpoints")
public class AuthController {

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @GetMapping("/public/health")
    @Operation(summary = "Public health check")
    public ResponseEntity<?> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/profile")
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        Map<String, Object> profile = new HashMap<>();
        
        profile.put("username", keycloakJwtUtils.getUsernameFromJwtToken(authentication));
        profile.put("email", keycloakJwtUtils.getEmailFromJwtToken(authentication));
        profile.put("userId", keycloakJwtUtils.getUserIdFromJwtToken(authentication));
        profile.put("roles", keycloakJwtUtils.getRolesFromJwtToken(authentication));
        profile.put("realmRoles", keycloakJwtUtils.getRealmRolesFromJwtToken(authentication));
        
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/auth/user-info")
    @Operation(summary = "Get detailed user information", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        Map<String, Object> userInfo = keycloakJwtUtils.getAllClaimsFromJwtToken(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Get all users (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            var users = keycloakAdminService.getAllUsers(0, 50);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch users");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/admin/users")
    @Operation(summary = "Create new user (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            String userId = keycloakAdminService.createUser(
                request.getUsername(),
                request.getEmail(), 
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
            );

            // Assign default role if provided
            if (request.getRole() != null && !request.getRole().isEmpty()) {
                keycloakAdminService.assignRoleToUser(userId, request.getRole());
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("userId", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create user");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/auth/validate")
    @Operation(summary = "Validate JWT token", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = keycloakJwtUtils.validateJwtToken(authentication);
        response.put("valid", isValid);
        response.put("username", keycloakJwtUtils.getUsernameFromJwtToken(authentication));
        
        return ResponseEntity.ok(response);
    }

    // DTO for create user request
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private String role;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}