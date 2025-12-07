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

import com.smart.edilek.entity.lookup.PetitionPreferencesDateFormat;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/lookup/petition-preferences-date-format")
@Tag(name = "Petition Preferences Date Format Lookup", description = "Petition preferences date format lookup endpoints")
public class PetitionPreferencesDateFormatController {

    @Autowired
    private GenericServiceImp<PetitionPreferencesDateFormat> petitionPreferencesDateFormatService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition preferences date format", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionPreferencesDateFormat petitionPreferencesDateFormat, Authentication authentication) {
        try {
            petitionPreferencesDateFormatService.add(petitionPreferencesDateFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesDateFormat, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition preferences date format", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionPreferencesDateFormat petitionPreferencesDateFormat, Authentication authentication) {
        try {
            petitionPreferencesDateFormat = petitionPreferencesDateFormatService.modify(petitionPreferencesDateFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesDateFormat, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition preferences date format by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionPreferencesDateFormat> get(@PathVariable String id) {
        PetitionPreferencesDateFormat petitionPreferencesDateFormat = null;
        try {
            petitionPreferencesDateFormat = petitionPreferencesDateFormatService.get(PetitionPreferencesDateFormat.class, Long.parseLong(id));
            if (petitionPreferencesDateFormat == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionPreferencesDateFormat>(petitionPreferencesDateFormat, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition preferences date formats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionPreferencesDateFormat>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionPreferencesDateFormat> list = null;
        long count = 0;
        try {
            list = petitionPreferencesDateFormatService.find(PetitionPreferencesDateFormat.class, lazyEvent);
            count = petitionPreferencesDateFormatService.count(PetitionPreferencesDateFormat.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionPreferencesDateFormat> dataTableDto = new DataTableDto<PetitionPreferencesDateFormat>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionPreferencesDateFormat>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition preferences date formats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionPreferencesDateFormat>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionPreferencesDateFormat> list = petitionPreferencesDateFormatService.find(PetitionPreferencesDateFormat.class, lazyEvent);
            
            List<PetitionPreferencesDateFormat> activeList = list.stream()
                .filter(PetitionPreferencesDateFormat::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionPreferencesDateFormat>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition preferences date format (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionPreferencesDateFormat petitionPreferencesDateFormat = petitionPreferencesDateFormatService.get(PetitionPreferencesDateFormat.class, Long.parseLong(id));
            if (petitionPreferencesDateFormat == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionPreferencesDateFormat.setActive(false);
            
            petitionPreferencesDateFormat = petitionPreferencesDateFormatService.modify(petitionPreferencesDateFormat);
            
            MainDto dto = modelMapper.map(petitionPreferencesDateFormat, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
