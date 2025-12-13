package com.smart.edilek.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.core.annotation.LogExecutionTime;
import com.smart.edilek.entity.Company;
import com.smart.edilek.entity.User;
import com.smart.edilek.enums.CompanyRole;
import com.smart.edilek.models.CompanyDto;
import com.smart.edilek.models.UserDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.service.CompanyService;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/company")
@Tag(name = "Company Controller", description = "Company management endpoints")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/convert")
    @Operation(summary = "Convert personal account to corporate account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CompanyDto> convertAccountToCorporate(@RequestBody CompanyDto companyDto, Authentication authentication) {
        String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Company companyDetails = modelMapper.map(companyDto, Company.class);
        Company createdCompany = companyService.convertAccountToCorporate(userId, companyDetails);
        
        return new ResponseEntity<>(modelMapper.map(createdCompany, CompanyDto.class), HttpStatus.CREATED);
    }

    @PostMapping("/invite")
    @Operation(summary = "Invite user to company", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> inviteUser(@RequestParam String email, @RequestParam CompanyRole role, Authentication authentication) {
        String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        User currentUser = userGenericService.get(User.class, userId);
        
        if (currentUser == null || currentUser.getCompany() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        // Check if current user is OWNER or ADMIN
        if (currentUser.getCompanyRole() != CompanyRole.OWNER && currentUser.getCompanyRole() != CompanyRole.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        companyService.inviteUser(currentUser.getCompany().getId(), email, role);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/my-company")
    @Operation(summary = "Get current user's company", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CompanyDto> getMyCompany(Authentication authentication) {
        String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        User currentUser = userGenericService.get(User.class, userId);
        
        if (currentUser == null || currentUser.getCompany() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(modelMapper.map(currentUser.getCompany(), CompanyDto.class), HttpStatus.OK);
    }
    
    @GetMapping("/team")
    @Operation(summary = "Get company team members", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserDto>> getTeam(Authentication authentication) {
        String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        User currentUser = userGenericService.get(User.class, userId);
        
        if (currentUser == null || currentUser.getCompany() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<User> teamMembers = currentUser.getCompany().getUsers();
        List<UserDto> teamDtos = teamMembers.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
                
        return new ResponseEntity<>(teamDtos, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company information", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @RequestBody CompanyDto companyDto, Authentication authentication) {
        String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        User currentUser = userGenericService.get(User.class, userId);
        
        if (currentUser == null || currentUser.getCompany() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        if (!currentUser.getCompany().getId().equals(id)) {
             return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (currentUser.getCompanyRole() != CompanyRole.OWNER && currentUser.getCompanyRole() != CompanyRole.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Company companyDetails = modelMapper.map(companyDto, Company.class);
        Company updatedCompany = companyService.updateCompany(id, companyDetails);
        
        return new ResponseEntity<>(modelMapper.map(updatedCompany, CompanyDto.class), HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Remove user from company", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> removeUser(@PathVariable String userId, Authentication authentication) {
        String currentUserId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        User currentUser = userGenericService.get(User.class, currentUserId);
        
        if (currentUser == null || currentUser.getCompany() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        // Allow if user is removing themselves OR if user is OWNER/ADMIN
        boolean isSelfRemoval = currentUser.getId().equals(userId);
        boolean isAuthorized = currentUser.getCompanyRole() == CompanyRole.OWNER || currentUser.getCompanyRole() == CompanyRole.ADMIN;

        if (!isSelfRemoval && !isAuthorized) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        companyService.removeUserFromCompany(currentUser.getCompany().getId(), userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Update user role in company", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> updateUserRole(@PathVariable String userId, @RequestParam CompanyRole role, Authentication authentication) {
        String currentUserId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
        User currentUser = userGenericService.get(User.class, currentUserId);
        
        if (currentUser == null || currentUser.getCompany() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        // Only OWNER or ADMIN can update roles
        if (currentUser.getCompanyRole() != CompanyRole.OWNER && currentUser.getCompanyRole() != CompanyRole.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        // Prevent changing own role via this endpoint to avoid locking oneself out (optional but good practice)
        if (currentUser.getId().equals(userId)) {
             return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        companyService.updateUserRole(currentUser.getCompany().getId(), userId, role);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
