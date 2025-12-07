package com.smart.edilek.controller.lookup;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.lookup.PrivacyLevel;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/lookup/privacy-level")
@Tag(name = "Privacy Level Lookup", description = "Privacy level lookup endpoints")
public class PrivacyLevelController {

    @Autowired
    private GenericServiceImp<PrivacyLevel> privacyLevelService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new privacy level", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PrivacyLevel privacyLevel, Authentication authentication) {
        try {
            privacyLevelService.add(privacyLevel);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(privacyLevel, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing privacy level", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PrivacyLevel privacyLevel, Authentication authentication) {
        try {
            privacyLevel = privacyLevelService.modify(privacyLevel);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(privacyLevel, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get privacy level by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PrivacyLevel> get(@PathVariable String id) {
        PrivacyLevel privacyLevel = null;
        try {
            privacyLevel = privacyLevelService.get(PrivacyLevel.class, Long.parseLong(id));
            if (privacyLevel == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PrivacyLevel>(privacyLevel, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of privacy levels", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PrivacyLevel>> find(@RequestBody LazyEvent lazyEvent) {
        List<PrivacyLevel> list = null;
        long count = 0;
        try {
            list = privacyLevelService.find(PrivacyLevel.class, lazyEvent);
            count = privacyLevelService.count(PrivacyLevel.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PrivacyLevel> dataTableDto = new DataTableDto<PrivacyLevel>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PrivacyLevel>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active privacy levels", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrivacyLevel>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PrivacyLevel> list = privacyLevelService.find(PrivacyLevel.class, lazyEvent);
            
            List<PrivacyLevel> activeList = list.stream()
                .filter(PrivacyLevel::getActive)
                .toList();
            
            return new ResponseEntity<List<PrivacyLevel>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete privacy level (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PrivacyLevel privacyLevel = privacyLevelService.get(PrivacyLevel.class, Long.parseLong(id));
            if (privacyLevel == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            privacyLevel.setActive(false);
            
            privacyLevel = privacyLevelService.modify(privacyLevel);
            
            MainDto dto = modelMapper.map(privacyLevel, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
