package com.smart.edilek.controller;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

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

import com.smart.edilek.entity.UserOrder;
import com.smart.edilek.entity.User;
import com.smart.edilek.entity.LicensePackage;
import com.smart.edilek.entity.lookup.UserOrderStatus;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.models.UserOrderDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/userorder")
@Tag(name = "User Order Controller", description = "User order management endpoints")
public class UserOrderController {

    @Autowired
    private GenericServiceImp<UserOrder> userOrderGenericService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private GenericServiceImp<LicensePackage> licensePackageGenericService;

    @Autowired
    private GenericServiceImp<UserOrderStatus> userOrderStatusGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PutMapping(value = "/update-status/{id}/{statusId}")
    @Operation(summary = "Update user order status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> updateUserOrderStatus(@PathVariable Long id, @PathVariable Long statusId, Authentication authentication) {
        UserOrder userOrder = null;
        try {
            userOrder = userOrderGenericService.get(UserOrder.class, id);
            if (userOrder == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            UserOrderStatus status = userOrderStatusGenericService.get(UserOrderStatus.class, statusId);
            if (status == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            userOrder.setUserOrderStatus(status);
            
            if (authentication != null) {
                String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
                if (userId != null) {
                    userOrder.setUpdatedBy(userId);
                }
            }

            userOrder = userOrderGenericService.modify(userOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto userOrderDto = modelMapper.map(userOrder, MainDto.class);
        return new ResponseEntity<MainDto>(userOrderDto, HttpStatus.OK);
    }
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new user order", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addUserOrder(@RequestBody UserOrder userOrder, Authentication authentication) {
        try {
            // Set relationships
            if (userOrder.getUser() != null && userOrder.getUser().getId() != null && !userOrder.getUser().getId().isEmpty()) {
                User user = userGenericService.get(User.class, userOrder.getUser().getId());
                userOrder.setUser(user);
                if (user.getCompany() != null) {
                    userOrder.setCompany(user.getCompany());
                }
            }
            if (userOrder.getLicensePackage() != null && userOrder.getLicensePackage().getId() > 0) {
                LicensePackage licensePackage = licensePackageGenericService.get(LicensePackage.class, userOrder.getLicensePackage().getId());
                
                if (licensePackage == null) {
                    System.err.println("LicensePackage not found with ID: " + userOrder.getLicensePackage().getId());
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                
                userOrder.setLicensePackage(licensePackage);

                // Enforce Price and Currency from Package
                userOrder.setPrice(licensePackage.getPrice());
                userOrder.setCurrency(licensePackage.getCurrency());

                // Set expires_at based on duration_days
                if (licensePackage.getDurationDays() != null && licensePackage.getDurationDays() > 0) {
                    userOrder.setExpiresAt(LocalDateTime.now().plusDays(licensePackage.getDurationDays()));
                }
            } else {
                 System.err.println("LicensePackage ID is missing or 0");
                 return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            if (authentication != null) {
                String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
                if (userId != null) {
                    userOrder.setCreatedBy(userId);
                    userOrder.setUpdatedBy(userId);
                }
            }

            userOrderGenericService.add(userOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto userOrderDto = modelMapper.map(userOrder, MainDto.class);
        return new ResponseEntity<MainDto>(userOrderDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing user order", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyUserOrder(@RequestBody UserOrder userOrder, Authentication authentication) {
        try {
            // Set relationships
            if (userOrder.getUser() != null && userOrder.getUser().getId() != null && !userOrder.getUser().getId().isEmpty()) {
                User user = userGenericService.get(User.class, userOrder.getUser().getId());
                userOrder.setUser(user);
                if (user.getCompany() != null) {
                    userOrder.setCompany(user.getCompany());
                }
            }
            if (userOrder.getLicensePackage() != null && userOrder.getLicensePackage().getId() > 0) {
                userOrder.setLicensePackage(licensePackageGenericService.get(LicensePackage.class, userOrder.getLicensePackage().getId()));
            }
            
            if (authentication != null) {
                String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
                if (userId != null) {
                    userOrder.setUpdatedBy(userId);
                }
            }

            userOrder = userOrderGenericService.modify(userOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto userOrderDto = modelMapper.map(userOrder, MainDto.class);
        return new ResponseEntity<MainDto>(userOrderDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get user order by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserOrderDto> getUserOrder(@PathVariable String id) {
        UserOrder userOrder = null;
        try {
            userOrder = userOrderGenericService.get(UserOrder.class, Long.parseLong(id));
            if (userOrder == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        UserOrderDto userOrderDto = modelMapper.map(userOrder, UserOrderDto.class);
        return new ResponseEntity<UserOrderDto>(userOrderDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of user orders", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<UserOrderDto>> find(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<UserOrder> userOrderList = null;
        long count = 0;
        try {
            // Add user_id or company_id filter for authenticated user
            if (authentication != null) {
                String userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
                if (userId != null) {
                    Map<String, FilterMeta> filters = lazyEvent.getFilters();
                    if (filters == null) {
                        filters = new HashMap<>();
                        lazyEvent.setFilters(filters);
                    }
                    
                    User user = userGenericService.get(User.class, userId);
                    if (user != null && user.getCompany() != null) {
                         // Filter by company
                        Constraint constraint = new Constraint();
                        constraint.setValue(user.getCompany().getId());
                        constraint.setMatchMode("equals");
                        
                        List<Constraint> constraints = new ArrayList<>();
                        constraints.add(constraint);
                        
                        FilterMeta filterMeta = new FilterMeta();
                        filterMeta.setOperator("and");
                        filterMeta.setConstraints(constraints);
                        
                        filters.put("company.id", filterMeta);
                    } else {
                        // Filter by user
                        Constraint constraint = new Constraint();
                        constraint.setValue(userId);
                        constraint.setMatchMode("equals");
                        
                        List<Constraint> constraints = new ArrayList<>();
                        constraints.add(constraint);
                        
                        FilterMeta filterMeta = new FilterMeta();
                        filterMeta.setOperator("and");
                        filterMeta.setConstraints(constraints);
                        
                        filters.put("user.id", filterMeta);
                    }
                }
            }
            
            userOrderList = userOrderGenericService.find(UserOrder.class, lazyEvent);
            count = userOrderGenericService.count(UserOrder.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<UserOrderDto> dataTableDto = new DataTableDto<UserOrderDto>();
        List<UserOrderDto> userOrderDto = modelMapper.map(userOrderList, new TypeToken<List<UserOrderDto>>() {}.getType());
        dataTableDto.setData(userOrderDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<UserOrderDto>>(dataTableDto, HttpStatus.OK);
    }

    @PostMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get paginated list of user orders for admin", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<UserOrderDto>> findForAdmin(@RequestBody LazyEvent lazyEvent) {
        List<UserOrder> userOrderList = null;
        long count = 0;
        try {
            // Check if user.id filter exists and map to company if necessary
            if (lazyEvent.getFilters() != null && lazyEvent.getFilters().containsKey("user.id")) {
                FilterMeta filterMeta = lazyEvent.getFilters().get("user.id");
                if (filterMeta != null && filterMeta.getConstraints() != null && !filterMeta.getConstraints().isEmpty()) {
                    String userId = (String) filterMeta.getConstraints().get(0).getValue();
                    if (userId != null && !userId.isEmpty()) {
                        User user = userGenericService.get(User.class, userId);
                        if (user != null && user.getCompany() != null) {
                            // User belongs to a company, filter by company id instead
                            // Remove user.id filter
                            lazyEvent.getFilters().remove("user.id");
                            
                            // Add company.id filter
                            FilterMeta companyFilter = new FilterMeta();
                            companyFilter.setOperator("and");
                            List<Constraint> constraints = new ArrayList<>();
                            Constraint constraint = new Constraint();
                            constraint.setValue(user.getCompany().getId().toString());
                            constraint.setMatchMode("equals");
                            constraints.add(constraint);
                            companyFilter.setConstraints(constraints);
                            
                            lazyEvent.getFilters().put("company.id", companyFilter);
                        }
                    }
                }
            }

            userOrderList = userOrderGenericService.find(UserOrder.class, lazyEvent);
            count = userOrderGenericService.count(UserOrder.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<UserOrderDto> dataTableDto = new DataTableDto<UserOrderDto>();
        List<UserOrderDto> userOrderDto = modelMapper.map(userOrderList, new TypeToken<List<UserOrderDto>>() {}.getType());
        dataTableDto.setData(userOrderDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<UserOrderDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active user orders", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserOrderDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<UserOrder> userOrderList = userOrderGenericService.find(UserOrder.class, lazyEvent);
            
            List<UserOrder> activeList = userOrderList.stream()
                .filter(order -> Boolean.TRUE.equals(order.getActive()) && 
                        (order.getExpiresAt() == null || order.getExpiresAt().isAfter(LocalDateTime.now())))
                .toList();
            
            List<UserOrderDto> userOrderDto = modelMapper.map(activeList, new TypeToken<List<UserOrderDto>>() {}.getType());
            
            return new ResponseEntity<List<UserOrderDto>>(userOrderDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete user order (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deleteUserOrder(@PathVariable String id, Authentication authentication) {
        try {
            UserOrder userOrder = userOrderGenericService.get(UserOrder.class, Long.parseLong(id));
            if (userOrder == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            userOrder.setActive(false);
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    userOrder.setUpdatedBy(username);
                }
            }
            
            userOrder = userOrderGenericService.modify(userOrder);
            
            MainDto userOrderDto = modelMapper.map(userOrder, MainDto.class);
            return new ResponseEntity<MainDto>(userOrderDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}