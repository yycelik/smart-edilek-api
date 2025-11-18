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

import com.smart.edilek.entity.BasicApplication;
import com.smart.edilek.entity.Type;
import com.smart.edilek.entity.Firm;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.BasicApplicationDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/basicapplication")
@Tag(name = "Basic Application Controller", description = "Basic application management endpoints")
public class BasicApplicationController {

    @Autowired
    private GenericServiceImp<BasicApplication> basicApplicationGenericService;
    
    @Autowired
    private GenericServiceImp<Type> typeGenericService;
    
    @Autowired
    private GenericServiceImp<Firm> firmGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new basic application", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addBasicApplication(@RequestBody BasicApplication basicApplication, Authentication authentication) {
        try {
            // Set relationships
            if (basicApplication.getType() != null && basicApplication.getType().getId() > 0) {
                basicApplication.setType(typeGenericService.get(Type.class, basicApplication.getType().getId()));
            }
            if (basicApplication.getFirm() != null && basicApplication.getFirm().getId() > 0) {
                basicApplication.setFirm(firmGenericService.get(Firm.class, basicApplication.getFirm().getId()));
            }
            
            // Set creator information if available
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    basicApplication.setCreatedBy(username);
                    basicApplication.setUpdatedBy(username);
                }
            }

            basicApplicationGenericService.add(basicApplication);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto basicApplicationDto = modelMapper.map(basicApplication, MainDto.class);
        return new ResponseEntity<MainDto>(basicApplicationDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing basic application", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyBasicApplication(@RequestBody BasicApplication basicApplication, Authentication authentication) {
        try {
            // Set relationships
            if (basicApplication.getType() != null && basicApplication.getType().getId() > 0) {
                basicApplication.setType(typeGenericService.get(Type.class, basicApplication.getType().getId()));
            }
            if (basicApplication.getFirm() != null && basicApplication.getFirm().getId() > 0) {
                basicApplication.setFirm(firmGenericService.get(Firm.class, basicApplication.getFirm().getId()));
            }
            
            // Set updater information if available
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    basicApplication.setUpdatedBy(username);
                }
            }

            basicApplication = basicApplicationGenericService.modify(basicApplication);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto basicApplicationDto = modelMapper.map(basicApplication, MainDto.class);
        return new ResponseEntity<MainDto>(basicApplicationDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get basic application by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BasicApplicationDto> getBasicApplication(@PathVariable long id) {
        BasicApplication basicApplication = null;
        try {
            basicApplication = basicApplicationGenericService.get(BasicApplication.class, id);
            if (basicApplication == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        BasicApplicationDto basicApplicationDto = modelMapper.map(basicApplication, BasicApplicationDto.class);
        return new ResponseEntity<BasicApplicationDto>(basicApplicationDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of basic applications", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<BasicApplicationDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<BasicApplication> basicApplicationList = null;
        long count = 0;
        try {
            basicApplicationList = basicApplicationGenericService.find(BasicApplication.class, lazyEvent);
            count = basicApplicationGenericService.count(BasicApplication.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<BasicApplicationDto> dataTableDto = new DataTableDto<BasicApplicationDto>();
        List<BasicApplicationDto> basicApplicationDto = modelMapper.map(basicApplicationList, new TypeToken<List<BasicApplicationDto>>() {}.getType());
        dataTableDto.setData(basicApplicationDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<BasicApplicationDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active basic applications", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<BasicApplicationDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<BasicApplication> basicApplicationList = basicApplicationGenericService.find(BasicApplication.class, lazyEvent);
            
            List<BasicApplication> activeList = basicApplicationList.stream()
                .filter(BasicApplication::isActive)
                .toList();
            
            List<BasicApplicationDto> basicApplicationDto = modelMapper.map(activeList, new TypeToken<List<BasicApplicationDto>>() {}.getType());
            
            return new ResponseEntity<List<BasicApplicationDto>>(basicApplicationDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete basic application (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deleteBasicApplication(@PathVariable long id, Authentication authentication) {
        try {
            BasicApplication basicApplication = basicApplicationGenericService.get(BasicApplication.class, id);
            if (basicApplication == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            basicApplication.setActive(false);
            
            // Set updater information if available
            if (authentication != null) {
                String username = keycloakJwtUtils.getUsernameFromJwtToken(authentication);
                if (username != null) {
                    basicApplication.setUpdatedBy(username);
                }
            }
            
            basicApplication = basicApplicationGenericService.modify(basicApplication);
            
            MainDto basicApplicationDto = modelMapper.map(basicApplication, MainDto.class);
            return new ResponseEntity<MainDto>(basicApplicationDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}