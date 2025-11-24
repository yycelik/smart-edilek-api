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

import com.smart.edilek.entity.PetitionAttachmentEvidenceType;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionAttachmentEvidenceTypeDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/petition-attachment-evidence-type")
@Tag(name = "Petition Attachment Evidence Type Controller", description = "Petition attachment evidence type management endpoints")
public class PetitionAttachmentEvidenceTypeController {

    @Autowired
    private GenericServiceImp<PetitionAttachmentEvidenceType> petitionAttachmentEvidenceTypeGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition attachment evidence type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> add(@RequestBody PetitionAttachmentEvidenceType petitionAttachmentEvidenceType, Authentication authentication) {
        try {
            petitionAttachmentEvidenceTypeGenericService.add(petitionAttachmentEvidenceType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionAttachmentEvidenceType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition attachment evidence type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modify(@RequestBody PetitionAttachmentEvidenceType petitionAttachmentEvidenceType, Authentication authentication) {
        try {
            petitionAttachmentEvidenceType = petitionAttachmentEvidenceTypeGenericService.modify(petitionAttachmentEvidenceType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionAttachmentEvidenceType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition attachment evidence type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionAttachmentEvidenceTypeDto> get(@PathVariable String id) {
        PetitionAttachmentEvidenceType petitionAttachmentEvidenceType = null;
        try {
            petitionAttachmentEvidenceType = petitionAttachmentEvidenceTypeGenericService.get(PetitionAttachmentEvidenceType.class, id);
            if (petitionAttachmentEvidenceType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionAttachmentEvidenceTypeDto dto = modelMapper.map(petitionAttachmentEvidenceType, PetitionAttachmentEvidenceTypeDto.class);
        return new ResponseEntity<PetitionAttachmentEvidenceTypeDto>(dto, HttpStatus.OK);
    }

    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition attachment evidence types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionAttachmentEvidenceTypeDto>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionAttachmentEvidenceType> list = null;
        long count = 0;
        try {
            list = petitionAttachmentEvidenceTypeGenericService.find(PetitionAttachmentEvidenceType.class, lazyEvent);
            count = petitionAttachmentEvidenceTypeGenericService.count(PetitionAttachmentEvidenceType.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionAttachmentEvidenceTypeDto> dataTableDto = new DataTableDto<PetitionAttachmentEvidenceTypeDto>();
        List<PetitionAttachmentEvidenceTypeDto> dtoList = modelMapper.map(list, new TypeToken<List<PetitionAttachmentEvidenceTypeDto>>() {}.getType());
        dataTableDto.setData(dtoList);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionAttachmentEvidenceTypeDto>>(dataTableDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition attachment evidence type (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionAttachmentEvidenceType petitionAttachmentEvidenceType = petitionAttachmentEvidenceTypeGenericService.get(PetitionAttachmentEvidenceType.class, id);
            if (petitionAttachmentEvidenceType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            petitionAttachmentEvidenceType.setActive(false);
            petitionAttachmentEvidenceType = petitionAttachmentEvidenceTypeGenericService.modify(petitionAttachmentEvidenceType);
            
            MainDto dto = modelMapper.map(petitionAttachmentEvidenceType, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
