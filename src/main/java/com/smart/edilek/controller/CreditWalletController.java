package com.smart.edilek.controller;

import java.util.List;

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

import com.smart.edilek.entity.CreditWallet;
import com.smart.edilek.entity.User;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.CreditWalletDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new credit wallet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addCreditWallet(@RequestBody CreditWallet creditWallet, Authentication authentication) {
        try {
            // Set relationships
            if (creditWallet.getUser() != null && creditWallet.getUser().getId() != null && !creditWallet.getUser().getId().isEmpty()) {
                creditWallet.setUser(userGenericService.get(User.class, creditWallet.getUser().getId()));
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
            // Set relationships
            if (creditWallet.getUser() != null && creditWallet.getUser().getId() != null && !creditWallet.getUser().getId().isEmpty()) {
                creditWallet.setUser(userGenericService.get(User.class, creditWallet.getUser().getId()));
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
    public ResponseEntity<CreditWalletDto> getCreditWallet(@PathVariable String id) {
        CreditWallet creditWallet = null;
        try {
            creditWallet = creditWalletGenericService.get(CreditWallet.class, Long.parseLong(id));
            if (creditWallet == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        CreditWalletDto creditWalletDto = modelMapper.map(creditWallet, CreditWalletDto.class);
        return new ResponseEntity<CreditWalletDto>(creditWalletDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of credit wallets", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditWalletDto>> find(@RequestBody LazyEvent lazyEvent) {
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
    public ResponseEntity<List<CreditWalletDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
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