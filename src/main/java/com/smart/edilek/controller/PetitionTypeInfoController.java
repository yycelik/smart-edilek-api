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

import com.smart.edilek.entity.PetitionTypeInfo;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionTypeInfoDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petitiontypeinfo")
@Tag(name = "Petition Type Info Controller", description = "Petition type info management endpoints")
public class PetitionTypeInfoController {

    @Autowired
    private GenericServiceImp<PetitionTypeInfo> petitionTypeInfoGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition type info", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionTypeInfo(@RequestBody PetitionTypeInfo petitionTypeInfo, Authentication authentication) {
        try {
            // Set relationships
            if (petitionTypeInfo.getPetition() != null && petitionTypeInfo.getPetition().getId() != null) {
                petitionTypeInfo.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionTypeInfo.getPetition().getId())));
            }

            petitionTypeInfoGenericService.add(petitionTypeInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionTypeInfoDto = modelMapper.map(petitionTypeInfo, MainDto.class);
        return new ResponseEntity<MainDto>(petitionTypeInfoDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition type info", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionTypeInfo(@RequestBody PetitionTypeInfo petitionTypeInfo, Authentication authentication) {
        try {
            // Set relationships
            if (petitionTypeInfo.getPetition() != null && petitionTypeInfo.getPetition().getId() != null) {
                petitionTypeInfo.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionTypeInfo.getPetition().getId())));
            }

            petitionTypeInfo = petitionTypeInfoGenericService.modify(petitionTypeInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionTypeInfoDto = modelMapper.map(petitionTypeInfo, MainDto.class);
        return new ResponseEntity<MainDto>(petitionTypeInfoDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition type info by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionTypeInfoDto> getPetitionTypeInfo(@PathVariable String id) {
        PetitionTypeInfo petitionTypeInfo = null;
        try {
            petitionTypeInfo = petitionTypeInfoGenericService.get(PetitionTypeInfo.class, Long.parseLong(id));
            if (petitionTypeInfo == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionTypeInfoDto petitionTypeInfoDto = modelMapper.map(petitionTypeInfo, PetitionTypeInfoDto.class);
        return new ResponseEntity<PetitionTypeInfoDto>(petitionTypeInfoDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of petition type info", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionTypeInfoDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<PetitionTypeInfo> petitionTypeInfoList = null;
        long count = 0;
        try {
            petitionTypeInfoList = petitionTypeInfoGenericService.find(PetitionTypeInfo.class, lazyEvent);
            count = petitionTypeInfoGenericService.count(PetitionTypeInfo.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionTypeInfoDto> dataTableDto = new DataTableDto<PetitionTypeInfoDto>();
        List<PetitionTypeInfoDto> petitionTypeInfoDto = modelMapper.map(petitionTypeInfoList, new TypeToken<List<PetitionTypeInfoDto>>() {}.getType());
        dataTableDto.setData(petitionTypeInfoDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionTypeInfoDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition type info", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionTypeInfoDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionTypeInfo> petitionTypeInfoList = petitionTypeInfoGenericService.find(PetitionTypeInfo.class, lazyEvent);
            
            List<PetitionTypeInfo> activeList = petitionTypeInfoList.stream()
                .filter(PetitionTypeInfo::getActive)
                .toList();
            
            List<PetitionTypeInfoDto> petitionTypeInfoDto = modelMapper.map(activeList, new TypeToken<List<PetitionTypeInfoDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionTypeInfoDto>>(petitionTypeInfoDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition type info (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionTypeInfo(@PathVariable String id, Authentication authentication) {
        try {
            PetitionTypeInfo petitionTypeInfo = petitionTypeInfoGenericService.get(PetitionTypeInfo.class, Long.parseLong(id));
            if (petitionTypeInfo == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionTypeInfo.setActive(false);
            
            petitionTypeInfo = petitionTypeInfoGenericService.modify(petitionTypeInfo);
            
            MainDto petitionTypeInfoDto = modelMapper.map(petitionTypeInfo, MainDto.class);
            return new ResponseEntity<MainDto>(petitionTypeInfoDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}