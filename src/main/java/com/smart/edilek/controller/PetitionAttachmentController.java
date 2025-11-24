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

import com.smart.edilek.entity.PetitionAttachment;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionAttachmentDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petitionattachment")
@Tag(name = "Petition Attachment Controller", description = "Petition attachment management endpoints")
public class PetitionAttachmentController {

    @Autowired
    private GenericServiceImp<PetitionAttachment> petitionAttachmentGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetitionAttachment(@RequestBody PetitionAttachment petitionAttachment, Authentication authentication) {
        try {
            // Set relationships
            if (petitionAttachment.getPetition() != null && petitionAttachment.getPetition().getId() != null) {
                petitionAttachment.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionAttachment.getPetition().getId())));
            }

            petitionAttachmentGenericService.add(petitionAttachment);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionAttachmentDto = modelMapper.map(petitionAttachment, MainDto.class);
        return new ResponseEntity<MainDto>(petitionAttachmentDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition attachment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetitionAttachment(@RequestBody PetitionAttachment petitionAttachment, Authentication authentication) {
        try {
            // Set relationships
            if (petitionAttachment.getPetition() != null && petitionAttachment.getPetition().getId() != null) {
                petitionAttachment.setPetition(petitionGenericService.get(Petition.class, Long.parseLong(petitionAttachment.getPetition().getId())));
            }

            petitionAttachment = petitionAttachmentGenericService.modify(petitionAttachment);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionAttachmentDto = modelMapper.map(petitionAttachment, MainDto.class);
        return new ResponseEntity<MainDto>(petitionAttachmentDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition attachment by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionAttachmentDto> getPetitionAttachment(@PathVariable String id) {
        PetitionAttachment petitionAttachment = null;
        try {
            petitionAttachment = petitionAttachmentGenericService.get(PetitionAttachment.class, Long.parseLong(id));
            if (petitionAttachment == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionAttachmentDto petitionAttachmentDto = modelMapper.map(petitionAttachment, PetitionAttachmentDto.class);
        return new ResponseEntity<PetitionAttachmentDto>(petitionAttachmentDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionAttachmentDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionAttachment> petitionAttachmentList = null;
        long count = 0;
        try {
            petitionAttachmentList = petitionAttachmentGenericService.find(PetitionAttachment.class, lazyEvent);
            count = petitionAttachmentGenericService.count(PetitionAttachment.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionAttachmentDto> dataTableDto = new DataTableDto<PetitionAttachmentDto>();
        List<PetitionAttachmentDto> petitionAttachmentDto = modelMapper.map(petitionAttachmentList, new TypeToken<List<PetitionAttachmentDto>>() {}.getType());
        dataTableDto.setData(petitionAttachmentDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionAttachmentDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition attachments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionAttachmentDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionAttachment> petitionAttachmentList = petitionAttachmentGenericService.find(PetitionAttachment.class, lazyEvent);
            
            List<PetitionAttachment> activeList = petitionAttachmentList.stream()
                .filter(PetitionAttachment::getActive)
                .toList();
            
            List<PetitionAttachmentDto> petitionAttachmentDto = modelMapper.map(activeList, new TypeToken<List<PetitionAttachmentDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionAttachmentDto>>(petitionAttachmentDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition attachment (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetitionAttachment(@PathVariable String id, Authentication authentication) {
        try {
            PetitionAttachment petitionAttachment = petitionAttachmentGenericService.get(PetitionAttachment.class, Long.parseLong(id));
            if (petitionAttachment == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionAttachment.setActive(false);
            
            petitionAttachment = petitionAttachmentGenericService.modify(petitionAttachment);
            
            MainDto petitionAttachmentDto = modelMapper.map(petitionAttachment, MainDto.class);
            return new ResponseEntity<MainDto>(petitionAttachmentDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}