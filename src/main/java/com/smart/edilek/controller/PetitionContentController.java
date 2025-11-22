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

import com.smart.edilek.entity.PetitionContent;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionContentDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petitioncontent")
@Tag(name = "Petition Content Controller", description = "Petition content management endpoints")
public class PetitionContentController {

    @Autowired
    private GenericServiceImp<PetitionContent> petitionContentGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition content", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionContent(@RequestBody PetitionContent petitionContent, Authentication authentication) {
        try {
            // Set relationships
            if (petitionContent.getPetition() != null && petitionContent.getPetition().getId() != null) {
                petitionContent.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionContent.getPetition().getId())));
            }

            petitionContentGenericService.add(petitionContent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionContentDto = modelMapper.map(petitionContent, MainDto.class);
        return new ResponseEntity<MainDto>(petitionContentDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition content", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionContent(@RequestBody PetitionContent petitionContent, Authentication authentication) {
        try {
            // Set relationships
            if (petitionContent.getPetition() != null && petitionContent.getPetition().getId() != null) {
                petitionContent.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionContent.getPetition().getId())));
            }

            petitionContent = petitionContentGenericService.modify(petitionContent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionContentDto = modelMapper.map(petitionContent, MainDto.class);
        return new ResponseEntity<MainDto>(petitionContentDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition content by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionContentDto> getPetitionContent(@PathVariable String id) {
        PetitionContent petitionContent = null;
        try {
            petitionContent = petitionContentGenericService.get(PetitionContent.class, Long.parseLong(id));
            if (petitionContent == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionContentDto petitionContentDto = modelMapper.map(petitionContent, PetitionContentDto.class);
        return new ResponseEntity<PetitionContentDto>(petitionContentDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of petition content", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionContentDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<PetitionContent> petitionContentList = null;
        long count = 0;
        try {
            petitionContentList = petitionContentGenericService.find(PetitionContent.class, lazyEvent);
            count = petitionContentGenericService.count(PetitionContent.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionContentDto> dataTableDto = new DataTableDto<PetitionContentDto>();
        List<PetitionContentDto> petitionContentDto = modelMapper.map(petitionContentList, new TypeToken<List<PetitionContentDto>>() {}.getType());
        dataTableDto.setData(petitionContentDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionContentDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition content", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionContentDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionContent> petitionContentList = petitionContentGenericService.find(PetitionContent.class, lazyEvent);
            
            List<PetitionContent> activeList = petitionContentList.stream()
                .filter(PetitionContent::getActive)
                .toList();
            
            List<PetitionContentDto> petitionContentDto = modelMapper.map(activeList, new TypeToken<List<PetitionContentDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionContentDto>>(petitionContentDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition content (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionContent(@PathVariable String id, Authentication authentication) {
        try {
            PetitionContent petitionContent = petitionContentGenericService.get(PetitionContent.class, Long.parseLong(id));
            if (petitionContent == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionContent.setActive(false);
            
            petitionContent = petitionContentGenericService.modify(petitionContent);
            
            MainDto petitionContentDto = modelMapper.map(petitionContent, MainDto.class);
            return new ResponseEntity<MainDto>(petitionContentDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}