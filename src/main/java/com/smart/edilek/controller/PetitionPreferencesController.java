package com.smart.edilek.controller;

import com.smart.edilek.core.annotation.LogExecutionTime;
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

import com.smart.edilek.entity.PetitionPreferences;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionPreferencesDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/petitionpreferences")
@Tag(name = "Petition Preferences Controller", description = "Petition preferences management endpoints")
public class PetitionPreferencesController {

    @Autowired
    private GenericServiceImp<PetitionPreferences> petitionPreferencesGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionPreferences(@RequestBody PetitionPreferences petitionPreferences, Authentication authentication) {
        try {
            // Set relationships
            if (petitionPreferences.getPetition() != null && petitionPreferences.getPetition().getId() > 0) {
                petitionPreferences.setPetition(petitionGenericService.get(Petition.class, petitionPreferences.getPetition().getId()));
            }

            petitionPreferencesGenericService.add(petitionPreferences);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionPreferencesDto = modelMapper.map(petitionPreferences, MainDto.class);
        return new ResponseEntity<MainDto>(petitionPreferencesDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionPreferences(@RequestBody PetitionPreferences petitionPreferences, Authentication authentication) {
        try {
            // Set relationships
            if (petitionPreferences.getPetition() != null && petitionPreferences.getPetition().getId() > 0) {
                petitionPreferences.setPetition(petitionGenericService.get(Petition.class, petitionPreferences.getPetition().getId()));
            }

            petitionPreferences = petitionPreferencesGenericService.modify(petitionPreferences);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionPreferencesDto = modelMapper.map(petitionPreferences, MainDto.class);
        return new ResponseEntity<MainDto>(petitionPreferencesDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition preferences by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionPreferencesDto> getPetitionPreferences(@PathVariable String id) {
        PetitionPreferences petitionPreferences = null;
        try {
            petitionPreferences = petitionPreferencesGenericService.get(PetitionPreferences.class, Long.parseLong(id));
            if (petitionPreferences == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionPreferencesDto petitionPreferencesDto = modelMapper.map(petitionPreferences, PetitionPreferencesDto.class);
        return new ResponseEntity<PetitionPreferencesDto>(petitionPreferencesDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionPreferencesDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionPreferences> petitionPreferencesList = null;
        long count = 0;
        try {
            petitionPreferencesList = petitionPreferencesGenericService.find(PetitionPreferences.class, lazyEvent);
            count = petitionPreferencesGenericService.count(PetitionPreferences.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionPreferencesDto> dataTableDto = new DataTableDto<PetitionPreferencesDto>();
        List<PetitionPreferencesDto> petitionPreferencesDto = modelMapper.map(petitionPreferencesList, new TypeToken<List<PetitionPreferencesDto>>() {}.getType());
        dataTableDto.setData(petitionPreferencesDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionPreferencesDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionPreferencesDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionPreferences> petitionPreferencesList = petitionPreferencesGenericService.find(PetitionPreferences.class, lazyEvent);
            
            List<PetitionPreferences> activeList = petitionPreferencesList.stream()
                .filter(PetitionPreferences::getActive)
                .toList();
            
            List<PetitionPreferencesDto> petitionPreferencesDto = modelMapper.map(activeList, new TypeToken<List<PetitionPreferencesDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionPreferencesDto>>(petitionPreferencesDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition preferences (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionPreferences(@PathVariable String id, Authentication authentication) {
        try {
            PetitionPreferences petitionPreferences = petitionPreferencesGenericService.get(PetitionPreferences.class, Long.parseLong(id));
            if (petitionPreferences == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionPreferences.setActive(false);
            
            petitionPreferences = petitionPreferencesGenericService.modify(petitionPreferences);
            
            MainDto petitionPreferencesDto = modelMapper.map(petitionPreferences, MainDto.class);
            return new ResponseEntity<MainDto>(petitionPreferencesDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}