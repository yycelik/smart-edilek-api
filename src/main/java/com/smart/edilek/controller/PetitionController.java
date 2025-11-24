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

import com.smart.edilek.entity.Petition;
import com.smart.edilek.entity.User;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petition")
@Tag(name = "Petition Controller", description = "Petition management endpoints")
public class PetitionController {

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetition(@RequestBody Petition petition, Authentication authentication) {
        try {
            // Set relationships
            if (petition.getUser() != null && petition.getUser().getId() != null) {
                petition.setUser(userGenericService.get(User.class, Long.parseLong(petition.getUser().getId())));
            }

            petitionGenericService.add(petition);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionDto = modelMapper.map(petition, MainDto.class);
        return new ResponseEntity<MainDto>(petitionDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetition(@RequestBody Petition petition, Authentication authentication) {
        try {
            // Set relationships
            if (petition.getUser() != null && petition.getUser().getId() != null) {
                petition.setUser(userGenericService.get(User.class, Long.parseLong(petition.getUser().getId())));
            }

            petition = petitionGenericService.modify(petition);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionDto = modelMapper.map(petition, MainDto.class);
        return new ResponseEntity<MainDto>(petitionDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionDto> getPetition(@PathVariable String id) {
        Petition petition = null;
        try {
            petition = petitionGenericService.get(Petition.class, Long.parseLong(id));
            if (petition == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionDto petitionDto = modelMapper.map(petition, PetitionDto.class);
        return new ResponseEntity<PetitionDto>(petitionDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petitions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<Petition> petitionList = null;
        long count = 0;
        try {
            petitionList = petitionGenericService.find(Petition.class, lazyEvent);
            count = petitionGenericService.count(Petition.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionDto> dataTableDto = new DataTableDto<PetitionDto>();
        List<PetitionDto> petitionDto = modelMapper.map(petitionList, new TypeToken<List<PetitionDto>>() {}.getType());
        dataTableDto.setData(petitionDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petitions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<Petition> petitionList = petitionGenericService.find(Petition.class, lazyEvent);
            
            List<Petition> activeList = petitionList.stream()
                .filter(Petition::getActive)
                .toList();
            
            List<PetitionDto> petitionDto = modelMapper.map(activeList, new TypeToken<List<PetitionDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionDto>>(petitionDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetition(@PathVariable String id, Authentication authentication) {
        try {
            Petition petition = petitionGenericService.get(Petition.class, Long.parseLong(id));
            if (petition == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petition.setActive(false);
            
            petition = petitionGenericService.modify(petition);
            
            MainDto petitionDto = modelMapper.map(petition, MainDto.class);
            return new ResponseEntity<MainDto>(petitionDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}