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

import com.smart.edilek.entity.PetitionIdentity;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionIdentityDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petitionidentity")
@Tag(name = "Petition Identity Controller", description = "Petition identity management endpoints")
public class PetitionIdentityController {

    @Autowired
    private GenericServiceImp<PetitionIdentity> petitionIdentityGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition identity", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionIdentity(@RequestBody PetitionIdentity petitionIdentity, Authentication authentication) {
        try {
            // Set relationships
            if (petitionIdentity.getPetition() != null && petitionIdentity.getPetition().getId() != null) {
                petitionIdentity.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionIdentity.getPetition().getId())));
            }

            petitionIdentityGenericService.add(petitionIdentity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionIdentityDto = modelMapper.map(petitionIdentity, MainDto.class);
        return new ResponseEntity<MainDto>(petitionIdentityDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition identity", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionIdentity(@RequestBody PetitionIdentity petitionIdentity, Authentication authentication) {
        try {
            // Set relationships
            if (petitionIdentity.getPetition() != null && petitionIdentity.getPetition().getId() != null) {
                petitionIdentity.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionIdentity.getPetition().getId())));
            }

            petitionIdentity = petitionIdentityGenericService.modify(petitionIdentity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionIdentityDto = modelMapper.map(petitionIdentity, MainDto.class);
        return new ResponseEntity<MainDto>(petitionIdentityDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition identity by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionIdentityDto> getPetitionIdentity(@PathVariable String id) {
        PetitionIdentity petitionIdentity = null;
        try {
            petitionIdentity = petitionIdentityGenericService.get(PetitionIdentity.class, Long.parseLong(id));
            if (petitionIdentity == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionIdentityDto petitionIdentityDto = modelMapper.map(petitionIdentity, PetitionIdentityDto.class);
        return new ResponseEntity<PetitionIdentityDto>(petitionIdentityDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition identities", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionIdentityDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionIdentity> petitionIdentityList = null;
        long count = 0;
        try {
            petitionIdentityList = petitionIdentityGenericService.find(PetitionIdentity.class, lazyEvent);
            count = petitionIdentityGenericService.count(PetitionIdentity.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionIdentityDto> dataTableDto = new DataTableDto<PetitionIdentityDto>();
        List<PetitionIdentityDto> petitionIdentityDto = modelMapper.map(petitionIdentityList, new TypeToken<List<PetitionIdentityDto>>() {}.getType());
        dataTableDto.setData(petitionIdentityDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionIdentityDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition identities", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionIdentityDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionIdentity> petitionIdentityList = petitionIdentityGenericService.find(PetitionIdentity.class, lazyEvent);
            
            List<PetitionIdentity> activeList = petitionIdentityList.stream()
                .filter(PetitionIdentity::getActive)
                .toList();
            
            List<PetitionIdentityDto> petitionIdentityDto = modelMapper.map(activeList, new TypeToken<List<PetitionIdentityDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionIdentityDto>>(petitionIdentityDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition identity (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionIdentity(@PathVariable String id, Authentication authentication) {
        try {
            PetitionIdentity petitionIdentity = petitionIdentityGenericService.get(PetitionIdentity.class, Long.parseLong(id));
            if (petitionIdentity == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionIdentity.setActive(false);
            
            petitionIdentity = petitionIdentityGenericService.modify(petitionIdentity);
            
            MainDto petitionIdentityDto = modelMapper.map(petitionIdentity, MainDto.class);
            return new ResponseEntity<MainDto>(petitionIdentityDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}