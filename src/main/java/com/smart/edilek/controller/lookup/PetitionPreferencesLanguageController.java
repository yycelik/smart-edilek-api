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

import com.smart.edilek.entity.lookup.PetitionPreferencesLanguage;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/lookup/petition-preferences-language")
@Tag(name = "Petition Preferences Language Lookup", description = "Petition preferences language lookup endpoints")
public class PetitionPreferencesLanguageController {

    @Autowired
    private GenericServiceImp<PetitionPreferencesLanguage> petitionPreferencesLanguageService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition preferences language", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionPreferencesLanguage petitionPreferencesLanguage, Authentication authentication) {
        try {
            petitionPreferencesLanguageService.add(petitionPreferencesLanguage);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesLanguage, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition preferences language", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionPreferencesLanguage petitionPreferencesLanguage, Authentication authentication) {
        try {
            petitionPreferencesLanguage = petitionPreferencesLanguageService.modify(petitionPreferencesLanguage);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesLanguage, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition preferences language by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionPreferencesLanguage> get(@PathVariable String id) {
        PetitionPreferencesLanguage petitionPreferencesLanguage = null;
        try {
            petitionPreferencesLanguage = petitionPreferencesLanguageService.get(PetitionPreferencesLanguage.class, Long.parseLong(id));
            if (petitionPreferencesLanguage == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionPreferencesLanguage>(petitionPreferencesLanguage, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition preferences languages", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionPreferencesLanguage>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionPreferencesLanguage> list = null;
        long count = 0;
        try {
            list = petitionPreferencesLanguageService.find(PetitionPreferencesLanguage.class, lazyEvent);
            count = petitionPreferencesLanguageService.count(PetitionPreferencesLanguage.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionPreferencesLanguage> dataTableDto = new DataTableDto<PetitionPreferencesLanguage>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionPreferencesLanguage>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition preferences languages", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionPreferencesLanguage>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionPreferencesLanguage> list = petitionPreferencesLanguageService.find(PetitionPreferencesLanguage.class, lazyEvent);
            
            List<PetitionPreferencesLanguage> activeList = list.stream()
                .filter(PetitionPreferencesLanguage::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionPreferencesLanguage>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition preferences language (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionPreferencesLanguage petitionPreferencesLanguage = petitionPreferencesLanguageService.get(PetitionPreferencesLanguage.class, Long.parseLong(id));
            if (petitionPreferencesLanguage == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionPreferencesLanguage.setActive(false);
            
            petitionPreferencesLanguage = petitionPreferencesLanguageService.modify(petitionPreferencesLanguage);
            
            MainDto dto = modelMapper.map(petitionPreferencesLanguage, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
