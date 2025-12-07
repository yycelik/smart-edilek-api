package com.smart.edilek.controller;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.User;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.UserDto;
import com.smart.edilek.models.UserSyncRequest;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.service.UserService;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "User management endpoints")
public class UserController {

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        Collection<String> roles = keycloakJwtUtils.getRealmRolesFromJwtToken(authentication);
        return roles != null && roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"));
    }

    private User getCurrentUser(Authentication authentication) {
        String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
        if (username == null) return null;
        List<User> users = userGenericService.find(User.class, "username", username, MatchMode.equals, 1);
        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }
    
    @PostMapping(value = "/sync")
    @Operation(summary = "Sync user from Keycloak to database", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> syncUser(@RequestBody UserSyncRequest request, Authentication authentication) {
        try {
            // Extract keycloakId from JWT token if not provided in request
            if (request.getKeycloakId() == null || request.getKeycloakId().isEmpty()) {
                if (authentication != null) {
                    String keycloakId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
                    request.setKeycloakId(keycloakId);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }

            // Extract user info from JWT token if not provided
            if (authentication != null) {
                if (request.getUsername() == null || request.getUsername().isEmpty()) {
                    String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                    request.setUsername(username);
                }
                if (request.getFirstname() == null || request.getFirstname().isEmpty()) {
                    String firstname = keycloakJwtUtils.getFirstNameFromJwtToken(authentication);
                    request.setFirstname(firstname);
                }
                if (request.getLastname() == null || request.getLastname().isEmpty()) {
                    String lastname = keycloakJwtUtils.getLastNameFromJwtToken(authentication);
                    request.setLastname(lastname);
                }
                if (request.getEmail() == null || request.getEmail().isEmpty()) {
                    String email = keycloakJwtUtils.getEmailFromJwtToken(authentication);
                    request.setEmail(email);
                }
            }

            // Sync user to database
            User user = userService.syncUserFromKeycloak(request);
            
            UserDto userDto = modelMapper.map(user, UserDto.class);
            return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> addUser(@RequestBody User user, Authentication authentication) {
        try {
            boolean isAdmin = isAdmin(authentication);
            if (!isAdmin) {
                 String tokenUsername = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                 if (tokenUsername == null || !tokenUsername.equals(user.getUsername())) {
                     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                 }
            }
            userGenericService.add(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        UserDto userDto = modelMapper.map(user, UserDto.class);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> modifyUser(@RequestBody User user, Authentication authentication) {
        try {
            boolean isAdmin = isAdmin(authentication);
            if (!isAdmin) {
                User currentUser = getCurrentUser(authentication);
                if (currentUser == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                
                // Check if trying to modify someone else
                if (user.getId() != null && !currentUser.getId().equals(user.getId())) {
                     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                // If ID is not provided in body but username is, check username
                if (user.getId() == null && user.getUsername() != null && !currentUser.getUsername().equals(user.getUsername())) {
                     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            user = userGenericService.modify(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        UserDto userDto = modelMapper.map(user, UserDto.class);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> getUser(@PathVariable String id, Authentication authentication) {
        User user = null;
        try {
            user = userGenericService.get(User.class, id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            boolean isAdmin = isAdmin(authentication);
            if (!isAdmin) {
                User currentUser = getCurrentUser(authentication);
                if (currentUser == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                if (!currentUser.getId().equals(user.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        UserDto userDto = modelMapper.map(user, UserDto.class);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<UserDto>> find(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<User> userList = null;
        long count = 0;
        try {
            if (!isAdmin(authentication)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            userList = userGenericService.find(User.class, lazyEvent);
            count = userGenericService.count(User.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<UserDto> dataTableDto = new DataTableDto<UserDto>();
        List<UserDto> userDto = modelMapper.map(userList, new TypeToken<List<UserDto>>() {}.getType());
        dataTableDto.setData(userDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<UserDto>>(dataTableDto, HttpStatus.OK);
    }



    @GetMapping("/list/all")
    @Operation(summary = "Get all active users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserDto>> getAllActive(Authentication authentication) {
        try {
            boolean isAdmin = isAdmin(authentication);
            User currentUser = null;
            if (!isAdmin) {
                currentUser = getCurrentUser(authentication);
                if (currentUser == null) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }

            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            if (!isAdmin) {
                 if (lazyEvent.getFilters() == null) {
                    lazyEvent.setFilters(new HashMap<>());
                }
                
                FilterMeta filterMeta = new FilterMeta();
                filterMeta.setOperator("and");
                List<Constraint> constraints = new ArrayList<>();
                Constraint constraint = new Constraint();
                constraint.setValue(currentUser.getUsername());
                constraint.setMatchMode(MatchMode.equals.toString());
                constraints.add(constraint);
                filterMeta.setConstraints(constraints);
                
                lazyEvent.getFilters().put("username", filterMeta);
            }
            
            List<User> userList = userGenericService.find(User.class, lazyEvent);
            
            List<User> activeList = userList.stream()
                .filter(User::getActive)
                .toList();
            
            List<UserDto> userDto = modelMapper.map(activeList, new TypeToken<List<UserDto>>() {}.getType());
            
            return new ResponseEntity<List<UserDto>>(userDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete user (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> deleteUser(@PathVariable String id, Authentication authentication) {
        try {
            User user = userGenericService.get(User.class, id);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            boolean isAdmin = isAdmin(authentication);
            if (!isAdmin) {
                User currentUser = getCurrentUser(authentication);
                if (currentUser == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                if (!currentUser.getId().equals(user.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            
            // Soft delete - just set active to false
            user.setActive(false);
            
            user = userGenericService.modify(user);
            
            UserDto userDto = modelMapper.map(user, UserDto.class);
            return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}