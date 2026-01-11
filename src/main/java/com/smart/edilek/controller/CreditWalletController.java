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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.CreditWallet;
import com.smart.edilek.entity.User;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.CreditWalletDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/creditwallet")
@Tag(name = "Credit Wallet Controller", description = "Credit wallet management endpoints")
public class CreditWalletController {

    @Autowired
    private GenericServiceImp<CreditWallet> creditWalletGenericService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

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
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new credit wallet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addCreditWallet(@RequestBody CreditWallet creditWallet, Authentication authentication) {
        try {
            boolean isAdmin = isAdmin(authentication);
            User currentUser = getCurrentUser(authentication);

            if (!isAdmin) {
                if (currentUser == null) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                creditWallet.setUser(currentUser);
            } else {
                // Set relationships
                if (creditWallet.getUser() != null && creditWallet.getUser().getId() != null && !creditWallet.getUser().getId().isEmpty()) {
                    creditWallet.setUser(userGenericService.get(User.class, creditWallet.getUser().getId()));
                }
            }
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    creditWallet.setCreatedBy(username);
                    creditWallet.setUpdatedBy(username);
                }
            }

            creditWalletGenericService.add(creditWallet);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto creditWalletDto = modelMapper.map(creditWallet, MainDto.class);
        return new ResponseEntity<MainDto>(creditWalletDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing credit wallet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyCreditWallet(@RequestBody CreditWallet creditWallet, Authentication authentication) {
        try {
            boolean isAdmin = isAdmin(authentication);
            User currentUser = getCurrentUser(authentication);

            CreditWallet existingWallet = creditWalletGenericService.get(CreditWallet.class, creditWallet.getId());
            if (existingWallet == null) {
                 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (!isAdmin) {
                if (currentUser == null) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                if (existingWallet.getUser() == null || !existingWallet.getUser().getId().equals(currentUser.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                creditWallet.setUser(currentUser);
            } else {
                // Set relationships
                if (creditWallet.getUser() != null && creditWallet.getUser().getId() != null && !creditWallet.getUser().getId().isEmpty()) {
                    creditWallet.setUser(userGenericService.get(User.class, creditWallet.getUser().getId()));
                }
            }
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    creditWallet.setUpdatedBy(username);
                }
            }

            creditWallet = creditWalletGenericService.modify(creditWallet);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto creditWalletDto = modelMapper.map(creditWallet, MainDto.class);
        return new ResponseEntity<MainDto>(creditWalletDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get credit wallet by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CreditWalletDto> getCreditWallet(@PathVariable String id, Authentication authentication) {
        CreditWallet creditWallet = null;
        try {
            creditWallet = creditWalletGenericService.get(CreditWallet.class, Long.parseLong(id));
            if (creditWallet == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            boolean isAdmin = isAdmin(authentication);
            if (!isAdmin) {
                User currentUser = getCurrentUser(authentication);
                if (currentUser == null) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                if (creditWallet.getUser() == null || !creditWallet.getUser().getId().equals(currentUser.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        CreditWalletDto creditWalletDto = modelMapper.map(creditWallet, CreditWalletDto.class);
        return new ResponseEntity<CreditWalletDto>(creditWalletDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of credit wallets for current user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditWalletDto>> find(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<CreditWallet> creditWalletList = null;
        long count = 0;
        try {
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            
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
            
            lazyEvent.getFilters().put("user.username", filterMeta);

            creditWalletList = creditWalletGenericService.find(CreditWallet.class, lazyEvent);
            count = creditWalletGenericService.count(CreditWallet.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<CreditWalletDto> dataTableDto = new DataTableDto<CreditWalletDto>();
        List<CreditWalletDto> creditWalletDto = modelMapper.map(creditWalletList, new TypeToken<List<CreditWalletDto>>() {}.getType());
        dataTableDto.setData(creditWalletDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<CreditWalletDto>>(dataTableDto, HttpStatus.OK);
    }

    @PostMapping("/admin/find")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Find credit wallets (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditWalletDto>> findForAdmin(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<CreditWallet> creditWalletList = null;
        long count = 0;
        try {
            // Admin doesn't need forced user filter
            creditWalletList = creditWalletGenericService.find(CreditWallet.class, lazyEvent);
            count = creditWalletGenericService.count(CreditWallet.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<CreditWalletDto> dataTableDto = new DataTableDto<CreditWalletDto>();
        List<CreditWalletDto> creditWalletDto = modelMapper.map(creditWalletList, new TypeToken<List<CreditWalletDto>>() {}.getType());
        dataTableDto.setData(creditWalletDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<CreditWalletDto>>(dataTableDto, HttpStatus.OK);
    }

    @PostMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get paginated list of all credit wallets (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditWalletDto>> findAll(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<CreditWallet> creditWalletList = null;
        long count = 0;
        try {
            creditWalletList = creditWalletGenericService.find(CreditWallet.class, lazyEvent);
            count = creditWalletGenericService.count(CreditWallet.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<CreditWalletDto> dataTableDto = new DataTableDto<CreditWalletDto>();
        List<CreditWalletDto> creditWalletDto = modelMapper.map(creditWalletList, new TypeToken<List<CreditWalletDto>>() {}.getType());
        dataTableDto.setData(creditWalletDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<CreditWalletDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active credit wallets", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CreditWalletDto>> getAllActive(Authentication authentication) {
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
                
                lazyEvent.getFilters().put("user.username", filterMeta);
            }
            
            List<CreditWallet> creditWalletList = creditWalletGenericService.find(CreditWallet.class, lazyEvent);
            
            List<CreditWallet> activeList = creditWalletList.stream()
                .filter(CreditWallet::getActive)
                .toList();
            
            List<CreditWalletDto> creditWalletDto = modelMapper.map(activeList, new TypeToken<List<CreditWalletDto>>() {}.getType());
            
            return new ResponseEntity<List<CreditWalletDto>>(creditWalletDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete credit wallet (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deleteCreditWallet(@PathVariable String id, Authentication authentication) {
        try {
            CreditWallet creditWallet = creditWalletGenericService.get(CreditWallet.class, Long.parseLong(id));
            if (creditWallet == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            boolean isAdmin = isAdmin(authentication);
            if (!isAdmin) {
                User currentUser = getCurrentUser(authentication);
                if (currentUser == null) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                if (creditWallet.getUser() == null || !creditWallet.getUser().getId().equals(currentUser.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            
            // Soft delete - just set active to false
            creditWallet.setActive(false);
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    creditWallet.setUpdatedBy(username);
                }
            }
            
            creditWallet = creditWalletGenericService.modify(creditWallet);
            
            MainDto creditWalletDto = modelMapper.map(creditWallet, MainDto.class);
            return new ResponseEntity<MainDto>(creditWalletDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}