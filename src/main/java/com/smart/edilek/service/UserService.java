package com.smart.edilek.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.service.GenericServiceImp;
import com.smart.edilek.entity.User;
import com.smart.edilek.models.UserSyncRequest;

@Service
public class UserService {

    @Autowired
    private GenericServiceImp<User> userGenericService;

    /**
     * Sync user from Keycloak to database
     * Creates user if not exists, updates if exists
     */
    @Transactional
    public User syncUserFromKeycloak(UserSyncRequest request) {
        User user = null;

        // Search for user by keycloakId
        if (request.getKeycloakId() != null && !request.getKeycloakId().isEmpty()) {
            List<User> users = userGenericService.find(
                User.class, 
                "id", 
                request.getKeycloakId(), 
                MatchMode.equals, 
                1
            );

            if (!users.isEmpty()) {
                user = users.get(0);
            }
        }

        // If user not found, create new one
        if (user == null) {
            user = new User();
            user.setId(request.getKeycloakId());
            user.setUsername(request.getUsername());
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setActive(true);
            
            userGenericService.add(user);
        } else {
            user.setUsername(request.getUsername());
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            
            user = userGenericService.modify(user);
        }

        return user;
    }
}
