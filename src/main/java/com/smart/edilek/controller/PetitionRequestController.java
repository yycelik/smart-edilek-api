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

import com.smart.edilek.entity.PetitionRequest;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionRequestDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petitionrequest")
@Tag(name = "Petition Request Controller", description = "Petition request management endpoints")
public class PetitionRequestController {

    @Autowired
    private GenericServiceImp<PetitionRequest> petitionRequestGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition request", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionRequest(@RequestBody PetitionRequest petitionRequest, Authentication authentication) {
        try {
            // Set relationships
            if (petitionRequest.getPetition() != null && petitionRequest.getPetition().getId() != null) {
                petitionRequest.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionRequest.getPetition().getId())));
            }

            petitionRequestGenericService.add(petitionRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionRequestDto = modelMapper.map(petitionRequest, MainDto.class);
        return new ResponseEntity<MainDto>(petitionRequestDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition request", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionRequest(@RequestBody PetitionRequest petitionRequest, Authentication authentication) {
        try {
            // Set relationships
            if (petitionRequest.getPetition() != null && petitionRequest.getPetition().getId() != null) {
                petitionRequest.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionRequest.getPetition().getId())));
            }

            petitionRequest = petitionRequestGenericService.modify(petitionRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionRequestDto = modelMapper.map(petitionRequest, MainDto.class);
        return new ResponseEntity<MainDto>(petitionRequestDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition request by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionRequestDto> getPetitionRequest(@PathVariable String id) {
        PetitionRequest petitionRequest = null;
        try {
            petitionRequest = petitionRequestGenericService.get(PetitionRequest.class, Long.parseLong(id));
            if (petitionRequest == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionRequestDto petitionRequestDto = modelMapper.map(petitionRequest, PetitionRequestDto.class);
        return new ResponseEntity<PetitionRequestDto>(petitionRequestDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition requests", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionRequestDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionRequest> petitionRequestList = null;
        long count = 0;
        try {
            petitionRequestList = petitionRequestGenericService.find(PetitionRequest.class, lazyEvent);
            count = petitionRequestGenericService.count(PetitionRequest.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionRequestDto> dataTableDto = new DataTableDto<PetitionRequestDto>();
        List<PetitionRequestDto> petitionRequestDto = modelMapper.map(petitionRequestList, new TypeToken<List<PetitionRequestDto>>() {}.getType());
        dataTableDto.setData(petitionRequestDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionRequestDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition requests", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionRequestDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionRequest> petitionRequestList = petitionRequestGenericService.find(PetitionRequest.class, lazyEvent);
            
            List<PetitionRequest> activeList = petitionRequestList.stream()
                .filter(PetitionRequest::getActive)
                .toList();
            
            List<PetitionRequestDto> petitionRequestDto = modelMapper.map(activeList, new TypeToken<List<PetitionRequestDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionRequestDto>>(petitionRequestDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition request (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionRequest(@PathVariable String id, Authentication authentication) {
        try {
            PetitionRequest petitionRequest = petitionRequestGenericService.get(PetitionRequest.class, Long.parseLong(id));
            if (petitionRequest == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionRequest.setActive(false);
            
            petitionRequest = petitionRequestGenericService.modify(petitionRequest);
            
            MainDto petitionRequestDto = modelMapper.map(petitionRequest, MainDto.class);
            return new ResponseEntity<MainDto>(petitionRequestDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}