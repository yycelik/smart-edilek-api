package com.smart.edilek.controller.lookup;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

import com.smart.edilek.entity.lookup.PetitionRequestType;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/petition-request-type")
@Tag(name = "Petition Request Type Lookup", description = "Petition request type lookup endpoints")
public class PetitionRequestTypeController {

    @Autowired
    private GenericServiceImp<PetitionRequestType> petitionRequestTypeService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition request type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionRequestType petitionRequestType, Authentication authentication) {
        try {
            petitionRequestTypeService.add(petitionRequestType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionRequestType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition request type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionRequestType petitionRequestType, Authentication authentication) {
        try {
            petitionRequestType = petitionRequestTypeService.modify(petitionRequestType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionRequestType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition request type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionRequestType> get(@PathVariable String id) {
        PetitionRequestType petitionRequestType = null;
        try {
            petitionRequestType = petitionRequestTypeService.get(PetitionRequestType.class, Long.parseLong(id));
            if (petitionRequestType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionRequestType>(petitionRequestType, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition request types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionRequestType>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionRequestType> list = null;
        long count = 0;
        try {
            list = petitionRequestTypeService.find(PetitionRequestType.class, lazyEvent);
            count = petitionRequestTypeService.count(PetitionRequestType.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionRequestType> dataTableDto = new DataTableDto<PetitionRequestType>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionRequestType>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition request types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionRequestType>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionRequestType> list = petitionRequestTypeService.find(PetitionRequestType.class, lazyEvent);
            
            List<PetitionRequestType> activeList = list.stream()
                .filter(PetitionRequestType::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionRequestType>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition request type (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionRequestType petitionRequestType = petitionRequestTypeService.get(PetitionRequestType.class, Long.parseLong(id));
            if (petitionRequestType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionRequestType.setActive(false);
            
            petitionRequestType = petitionRequestTypeService.modify(petitionRequestType);
            
            MainDto dto = modelMapper.map(petitionRequestType, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
