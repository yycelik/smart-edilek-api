package com.smart.edilek.controller;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;

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

import org.springframework.security.access.prepost.PreAuthorize;
import com.smart.edilek.entity.CreditTransaction;
import com.smart.edilek.entity.User;
import com.smart.edilek.entity.CreditWallet;
import com.smart.edilek.entity.UserOrder;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.models.CreditTransactionDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/credittransaction")
@Tag(name = "Credit Transaction Controller", description = "Credit transaction management endpoints")
public class CreditTransactionController {

    @Autowired
    private GenericServiceImp<CreditTransaction> creditTransactionGenericService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private GenericServiceImp<CreditWallet> creditWalletGenericService;

    @Autowired
    private GenericServiceImp<UserOrder> userOrderGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
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
    @Operation(summary = "Add new credit transaction", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addCreditTransaction(@RequestBody CreditTransaction creditTransaction, Authentication authentication) {
        try {
            // Set relationships
            if (creditTransaction.getUser() != null && creditTransaction.getUser().getId() != null && !creditTransaction.getUser().getId().isEmpty()) {
                creditTransaction.setUser(userGenericService.get(User.class, creditTransaction.getUser().getId()));
            }
            if (creditTransaction.getWallet() != null && creditTransaction.getWallet().getId() > 0) {
                creditTransaction.setWallet(creditWalletGenericService.get(CreditWallet.class, creditTransaction.getWallet().getId()));
            }
            if (creditTransaction.getUserOrder() != null && creditTransaction.getUserOrder().getId() > 0) {
                UserOrder order = userOrderGenericService.get(UserOrder.class, creditTransaction.getUserOrder().getId());

                // Expiration Check
                if (order != null && order.getExpiresAt() != null && order.getExpiresAt().isBefore(LocalDateTime.now())) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }

                creditTransaction.setUserOrder(order);
            }
            if (creditTransaction.getPetition() != null && creditTransaction.getPetition().getId() > 0) {
                creditTransaction.setPetition(petitionGenericService.get(Petition.class, creditTransaction.getPetition().getId()));
            }
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    creditTransaction.setCreatedBy(username);
                    creditTransaction.setUpdatedBy(username);
                }
            }

            creditTransactionGenericService.add(creditTransaction);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto creditTransactionDto = modelMapper.map(creditTransaction, MainDto.class);
        return new ResponseEntity<MainDto>(creditTransactionDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing credit transaction", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyCreditTransaction(@RequestBody CreditTransaction creditTransaction, Authentication authentication) {
        try {
            // Set relationships
            if (creditTransaction.getUser() != null && creditTransaction.getUser().getId() != null && !creditTransaction.getUser().getId().isEmpty()) {
                creditTransaction.setUser(userGenericService.get(User.class, creditTransaction.getUser().getId()));
            }
            if (creditTransaction.getWallet() != null && creditTransaction.getWallet().getId() > 0) {
                creditTransaction.setWallet(creditWalletGenericService.get(CreditWallet.class, creditTransaction.getWallet().getId()));
            }
            if (creditTransaction.getUserOrder() != null && creditTransaction.getUserOrder().getId() > 0) {
                creditTransaction.setUserOrder(userOrderGenericService.get(UserOrder.class, creditTransaction.getUserOrder().getId()));
            }
            if (creditTransaction.getPetition() != null && creditTransaction.getPetition().getId() > 0) {
                creditTransaction.setPetition(petitionGenericService.get(Petition.class, creditTransaction.getPetition().getId()));
            }
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    creditTransaction.setUpdatedBy(username);
                }
            }

            creditTransaction = creditTransactionGenericService.modify(creditTransaction);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto creditTransactionDto = modelMapper.map(creditTransaction, MainDto.class);
        return new ResponseEntity<MainDto>(creditTransactionDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get credit transaction by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CreditTransactionDto> getCreditTransaction(@PathVariable String id, Authentication authentication) {
        CreditTransaction creditTransaction = null;
        try {
            creditTransaction = creditTransactionGenericService.get(CreditTransaction.class, Long.parseLong(id));
            if (creditTransaction == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        CreditTransactionDto creditTransactionDto = modelMapper.map(creditTransaction, CreditTransactionDto.class);
        return new ResponseEntity<CreditTransactionDto>(creditTransactionDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of credit transactions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditTransactionDto>> find(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<CreditTransaction> creditTransactionList = null;
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
            
            if (currentUser.getCompany() != null) {
                // If user belongs to a company, show company transactions
                constraint.setValue(currentUser.getCompany().getId().toString());
                constraint.setMatchMode(MatchMode.equals.toString());
                constraints.add(constraint);
                filterMeta.setConstraints(constraints);
                lazyEvent.getFilters().put("wallet.company.id", filterMeta);
            } else {
                // Otherwise show user transactions
                constraint.setValue(currentUser.getUsername());
                constraint.setMatchMode(MatchMode.equals.toString());
                constraints.add(constraint);
                filterMeta.setConstraints(constraints);
                lazyEvent.getFilters().put("user.username", filterMeta);
            }

            creditTransactionList = creditTransactionGenericService.find(CreditTransaction.class, lazyEvent);
            count = creditTransactionGenericService.count(CreditTransaction.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<CreditTransactionDto> dataTableDto = new DataTableDto<CreditTransactionDto>();
        List<CreditTransactionDto> creditTransactionDto = modelMapper.map(creditTransactionList, new TypeToken<List<CreditTransactionDto>>() {}.getType());
        dataTableDto.setData(creditTransactionDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<CreditTransactionDto>>(dataTableDto, HttpStatus.OK);
    }

    @PostMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get paginated list of credit transactions for admin", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditTransactionDto>> findForAdmin(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<CreditTransaction> creditTransactionList = null;
        long count = 0;
        try {
            // Check if user.id filter exists and map to company wallet if necessary
            if (lazyEvent.getFilters() != null && lazyEvent.getFilters().containsKey("user.id")) {
                FilterMeta filterMeta = lazyEvent.getFilters().get("user.id");
                if (filterMeta != null && filterMeta.getConstraints() != null && !filterMeta.getConstraints().isEmpty()) {
                    String userId = (String) filterMeta.getConstraints().get(0).getValue();
                    if (userId != null && !userId.isEmpty()) {
                        User user = userGenericService.get(User.class, userId);
                        if (user != null && user.getCompany() != null) {
                            // User belongs to a company, filter by company wallet ID instead
                            // Remove user.id filter
                            lazyEvent.getFilters().remove("user.id");
                            
                            // Add wallet.company.id filter
                            FilterMeta companyFilter = new FilterMeta();
                            companyFilter.setOperator("and");
                            List<Constraint> constraints = new ArrayList<>();
                            Constraint constraint = new Constraint();
                            constraint.setValue(user.getCompany().getId().toString());
                            constraint.setMatchMode(MatchMode.equals.toString());
                            constraints.add(constraint);
                            companyFilter.setConstraints(constraints);
                            
                            lazyEvent.getFilters().put("wallet.company.id", companyFilter);
                        }
                        // If user doesn't belong to a company, we keep user.id filter (individual wallet works with user_id)
                    }
                }
            }

            creditTransactionList = creditTransactionGenericService.find(CreditTransaction.class, lazyEvent);
            count = creditTransactionGenericService.count(CreditTransaction.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<CreditTransactionDto> dataTableDto = new DataTableDto<CreditTransactionDto>();
        List<CreditTransactionDto> creditTransactionDto = modelMapper.map(creditTransactionList, new TypeToken<List<CreditTransactionDto>>() {}.getType());
        dataTableDto.setData(creditTransactionDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<CreditTransactionDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active credit transactions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CreditTransactionDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<CreditTransaction> creditTransactionList = creditTransactionGenericService.find(CreditTransaction.class, lazyEvent);
            
            List<CreditTransaction> activeList = creditTransactionList.stream()
                .filter(CreditTransaction::getActive)
                .toList();
            
            List<CreditTransactionDto> creditTransactionDto = modelMapper.map(activeList, new TypeToken<List<CreditTransactionDto>>() {}.getType());
            
            return new ResponseEntity<List<CreditTransactionDto>>(creditTransactionDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete credit transaction (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deleteCreditTransaction(@PathVariable String id, Authentication authentication) {
        try {
            CreditTransaction creditTransaction = creditTransactionGenericService.get(CreditTransaction.class, Long.parseLong(id));
            if (creditTransaction == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            creditTransaction.setActive(false);
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    creditTransaction.setUpdatedBy(username);
                }
            }
            
            creditTransaction = creditTransactionGenericService.modify(creditTransaction);
            
            MainDto creditTransactionDto = modelMapper.map(creditTransaction, MainDto.class);
            return new ResponseEntity<MainDto>(creditTransactionDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}