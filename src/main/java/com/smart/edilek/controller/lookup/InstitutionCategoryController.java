package com.smart.edilek.controller.lookup;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

import com.smart.edilek.entity.lookup.InstitutionCategory;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/institution-category")
@Tag(name = "Institution Category Lookup", description = "Institution category lookup endpoints")
public class InstitutionCategoryController {

    @Autowired
    private GenericServiceImp<InstitutionCategory> institutionCategoryService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new institution category", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody InstitutionCategory institutionCategory, Authentication authentication) {
        try {
            institutionCategoryService.add(institutionCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(institutionCategory, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing institution category", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody InstitutionCategory institutionCategory, Authentication authentication) {
        try {
            institutionCategory = institutionCategoryService.modify(institutionCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(institutionCategory, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get institution category by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<InstitutionCategory> get(@PathVariable String id) {
        InstitutionCategory institutionCategory = null;
        try {
            institutionCategory = institutionCategoryService.get(InstitutionCategory.class, Long.parseLong(id));
            if (institutionCategory == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<InstitutionCategory>(institutionCategory, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of institution categories", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<InstitutionCategory>> find(@RequestBody LazyEvent lazyEvent) {
        List<InstitutionCategory> list = null;
        long count = 0;
        try {
            list = institutionCategoryService.find(InstitutionCategory.class, lazyEvent);
            count = institutionCategoryService.count(InstitutionCategory.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<InstitutionCategory> dataTableDto = new DataTableDto<InstitutionCategory>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<InstitutionCategory>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active institution categories", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<InstitutionCategory>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<InstitutionCategory> list = institutionCategoryService.find(InstitutionCategory.class, lazyEvent);
            
            List<InstitutionCategory> activeList = list.stream()
                .filter(InstitutionCategory::getActive)
                .toList();
            
            return new ResponseEntity<List<InstitutionCategory>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete institution category (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            InstitutionCategory institutionCategory = institutionCategoryService.get(InstitutionCategory.class, Long.parseLong(id));
            if (institutionCategory == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            institutionCategory.setActive(false);
            
            institutionCategory = institutionCategoryService.modify(institutionCategory);
            
            MainDto dto = modelMapper.map(institutionCategory, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
