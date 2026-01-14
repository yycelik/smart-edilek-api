package com.smart.edilek.controller.lookup;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;

import org.modelmapper.ModelMapper;
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

import com.smart.edilek.entity.lookup.PetitionAiModel;
import com.smart.edilek.entity.UserOrder;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.service.GenericServiceImp;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/lookup/petition-ai-model")
@Tag(name = "Petition AI Model Lookup", description = "Petition AI Model lookup endpoints")
public class PetitionAiModelController {

    @Autowired
    private GenericServiceImp<PetitionAiModel> petitionAiModelService;

    @Autowired
    private GenericServiceImp<UserOrder> userOrderService;
    
    @Autowired(required = false)
    private KeycloakJwtUtils keycloakJwtUtils;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition ai model", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionAiModel petitionAiModel, Authentication authentication) {
        try {
            petitionAiModelService.add(petitionAiModel);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionAiModel, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition ai model", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionAiModel petitionAiModel, Authentication authentication) {
        try {
            petitionAiModel = petitionAiModelService.modify(petitionAiModel);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionAiModel, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition ai model by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionAiModel> get(@PathVariable String id) {
        PetitionAiModel petitionAiModel = null;
        try {
            petitionAiModel = petitionAiModelService.get(PetitionAiModel.class, Long.parseLong(id));
            if (petitionAiModel == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionAiModel>(petitionAiModel, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition ai models", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionAiModel>> find(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<PetitionAiModel> list = null;
        long count = 0;
        try {
            list = petitionAiModelService.find(PetitionAiModel.class, lazyEvent);
            count = petitionAiModelService.count(PetitionAiModel.class, lazyEvent);
            
            if (authentication != null && authentication.isAuthenticated()) {
                String userId = null;
                if (keycloakJwtUtils != null) {
                   userId = keycloakJwtUtils.getUserIdFromJwtToken(authentication);
                } else {
                   userId = authentication.getName();
                }
                
                if (userId != null) {
                    // Fetch user orders
                    LazyEvent orderEvent = new LazyEvent();
                    orderEvent.setFirst(0);
                    orderEvent.setRows(100); 
                    Map<String, FilterMeta> filters = new HashMap<>();
                    
                    Constraint constraint = new Constraint(userId, MatchMode.equals.name());
                    FilterMeta filterMeta = new FilterMeta("and", Collections.singletonList(constraint));
                    
                    filters.put("user.id", filterMeta);
                    orderEvent.setFilters(filters);
                    
                    List<UserOrder> userOrders = userOrderService.find(UserOrder.class, orderEvent);
                    
                    // Filter active orders
                    List<String> activePackageCodes = userOrders.stream()
                        .filter(o -> Boolean.TRUE.equals(o.getActive()))
                        .filter(o -> o.getExpiresAt() == null || o.getExpiresAt().isAfter(LocalDateTime.now()))
                        .map(o -> o.getLicensePackage().getCode())
                        .collect(Collectors.toList());
                        
                    // Filter Models
                    list = list.stream().filter(model -> {
                        String required = model.getRequiredPackageCodes();
                        if (required == null || required.trim().isEmpty()) {
                            return true; // No requirement
                        }
                        
                        String[] reqCodes = required.split(",");
                        for (String req : reqCodes) {
                            if (activePackageCodes.contains(req.trim())) {
                                return true;
                            }
                        }
                        return false;
                    }).collect(Collectors.toList());
                    
                    count = list.size();
                }
            } else {
                 list = list.stream().filter(model -> 
                    model.getRequiredPackageCodes() == null || model.getRequiredPackageCodes().trim().isEmpty()
                 ).collect(Collectors.toList());
                 count = list.size();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionAiModel> dataTableDto = new DataTableDto<PetitionAiModel>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionAiModel>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition ai models", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionAiModel>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionAiModel> list = petitionAiModelService.find(PetitionAiModel.class, lazyEvent);
            
            List<PetitionAiModel> activeList = list.stream()
                .filter(PetitionAiModel::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionAiModel>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition ai model (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionAiModel petitionAiModel = petitionAiModelService.get(PetitionAiModel.class, Long.parseLong(id));
            if (petitionAiModel == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionAiModel.setActive(false);
            
            petitionAiModel = petitionAiModelService.modify(petitionAiModel);
            
            MainDto dto = modelMapper.map(petitionAiModel, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
