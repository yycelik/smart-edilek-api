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

import com.smart.edilek.entity.PetitionAgreements;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionAgreementsDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petitionagreements")
@Tag(name = "Petition Agreements Controller", description = "Petition agreements management endpoints")
public class PetitionAgreementsController {

    @Autowired
    private GenericServiceImp<PetitionAgreements> petitionAgreementsGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition agreements", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionAgreements(@RequestBody PetitionAgreements petitionAgreements, Authentication authentication) {
        try {
            // Set relationships
            if (petitionAgreements.getPetition() != null && petitionAgreements.getPetition().getId() > 0) {
                petitionAgreements.setPetition(petitionGenericService.get(Petition.class, petitionAgreements.getPetition().getId()));
            }

            petitionAgreementsGenericService.add(petitionAgreements);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionAgreementsDto = modelMapper.map(petitionAgreements, MainDto.class);
        return new ResponseEntity<MainDto>(petitionAgreementsDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition agreements", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionAgreements(@RequestBody PetitionAgreements petitionAgreements, Authentication authentication) {
        try {
            // Set relationships
            if (petitionAgreements.getPetition() != null && petitionAgreements.getPetition().getId() > 0) {
                petitionAgreements.setPetition(petitionGenericService.get(Petition.class, petitionAgreements.getPetition().getId()));
            }

            petitionAgreements = petitionAgreementsGenericService.modify(petitionAgreements);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionAgreementsDto = modelMapper.map(petitionAgreements, MainDto.class);
        return new ResponseEntity<MainDto>(petitionAgreementsDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition agreements by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionAgreementsDto> getPetitionAgreements(@PathVariable String id) {
        PetitionAgreements petitionAgreements = null;
        try {
            petitionAgreements = petitionAgreementsGenericService.get(PetitionAgreements.class, Long.parseLong(id));
            if (petitionAgreements == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionAgreementsDto petitionAgreementsDto = modelMapper.map(petitionAgreements, PetitionAgreementsDto.class);
        return new ResponseEntity<PetitionAgreementsDto>(petitionAgreementsDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition agreements", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionAgreementsDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionAgreements> petitionAgreementsList = null;
        long count = 0;
        try {
            petitionAgreementsList = petitionAgreementsGenericService.find(PetitionAgreements.class, lazyEvent);
            count = petitionAgreementsGenericService.count(PetitionAgreements.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionAgreementsDto> dataTableDto = new DataTableDto<PetitionAgreementsDto>();
        List<PetitionAgreementsDto> petitionAgreementsDto = modelMapper.map(petitionAgreementsList, new TypeToken<List<PetitionAgreementsDto>>() {}.getType());
        dataTableDto.setData(petitionAgreementsDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionAgreementsDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition agreements", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionAgreementsDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionAgreements> petitionAgreementsList = petitionAgreementsGenericService.find(PetitionAgreements.class, lazyEvent);
            
            List<PetitionAgreements> activeList = petitionAgreementsList.stream()
                .filter(PetitionAgreements::getActive)
                .toList();
            
            List<PetitionAgreementsDto> petitionAgreementsDto = modelMapper.map(activeList, new TypeToken<List<PetitionAgreementsDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionAgreementsDto>>(petitionAgreementsDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition agreements (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionAgreements(@PathVariable String id, Authentication authentication) {
        try {
            PetitionAgreements petitionAgreements = petitionAgreementsGenericService.get(PetitionAgreements.class, Long.parseLong(id));
            if (petitionAgreements == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionAgreements.setActive(false);
            
            petitionAgreements = petitionAgreementsGenericService.modify(petitionAgreements);
            
            MainDto petitionAgreementsDto = modelMapper.map(petitionAgreements, MainDto.class);
            return new ResponseEntity<MainDto>(petitionAgreementsDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}