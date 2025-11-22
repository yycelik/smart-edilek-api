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

import com.smart.edilek.entity.LicensePackage;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.LicensePackageDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/license")
@Tag(name = "License Package Controller", description = "License package management endpoints")
public class LicenseController {

    @Autowired
    private GenericServiceImp<LicensePackage> licenseGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new license package", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addLicensePackage(@RequestBody LicensePackage licensePackage, Authentication authentication) {
        try {
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    licensePackage.setCreatedBy(username);
                    licensePackage.setUpdatedBy(username);
                }
            }

            licenseGenericService.add(licensePackage);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto licenseDto = modelMapper.map(licensePackage, MainDto.class);
        return new ResponseEntity<MainDto>(licenseDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing license package", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyLicensePackage(@RequestBody LicensePackage licensePackage, Authentication authentication) {
        try {
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    licensePackage.setUpdatedBy(username);
                }
            }

            licensePackage = licenseGenericService.modify(licensePackage);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto licenseDto = modelMapper.map(licensePackage, MainDto.class);
        return new ResponseEntity<MainDto>(licenseDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get license package by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LicensePackageDto> getLicensePackage(@PathVariable String id) {
        LicensePackage licensePackage = null;
        try {
            licensePackage = licenseGenericService.get(LicensePackage.class, Long.parseLong(id));
            if (licensePackage == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        LicensePackageDto licenseDto = modelMapper.map(licensePackage, LicensePackageDto.class);
        return new ResponseEntity<LicensePackageDto>(licenseDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of license packages", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<LicensePackageDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<LicensePackage> licenseList = null;
        long count = 0;
        try {
            licenseList = licenseGenericService.find(LicensePackage.class, lazyEvent);
            count = licenseGenericService.count(LicensePackage.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<LicensePackageDto> dataTableDto = new DataTableDto<LicensePackageDto>();
        List<LicensePackageDto> licenseDto = modelMapper.map(licenseList, new TypeToken<List<LicensePackageDto>>() {}.getType());
        dataTableDto.setData(licenseDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<LicensePackageDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active license packages", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<LicensePackageDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<LicensePackage> licenseList = licenseGenericService.find(LicensePackage.class, lazyEvent);
            
            List<LicensePackage> activeList = licenseList.stream()
                .filter(LicensePackage::getActive)
                .toList();
            
            List<LicensePackageDto> licenseDto = modelMapper.map(activeList, new TypeToken<List<LicensePackageDto>>() {}.getType());
            
            return new ResponseEntity<List<LicensePackageDto>>(licenseDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete license package (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deleteLicensePackage(@PathVariable String id, Authentication authentication) {
        try {
            LicensePackage licensePackage = licenseGenericService.get(LicensePackage.class, Long.parseLong(id));
            if (licensePackage == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            licensePackage.setActive(false);
            
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    licensePackage.setUpdatedBy(username);
                }
            }
            
            licensePackage = licenseGenericService.modify(licensePackage);
            
            MainDto licenseDto = modelMapper.map(licensePackage, MainDto.class);
            return new ResponseEntity<MainDto>(licenseDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}