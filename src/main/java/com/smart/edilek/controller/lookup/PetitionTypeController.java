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

import com.smart.edilek.entity.lookup.PetitionType;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/petition-type")
@Tag(name = "Petition Type Lookup", description = "Petition type lookup endpoints")
public class PetitionTypeController {

    @Autowired
    private GenericServiceImp<PetitionType> petitionTypeService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionType petitionType, Authentication authentication) {
        try {
            petitionTypeService.add(petitionType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionType petitionType, Authentication authentication) {
        try {
            petitionType = petitionTypeService.modify(petitionType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionType> get(@PathVariable String id) {
        PetitionType petitionType = null;
        try {
            petitionType = petitionTypeService.get(PetitionType.class, Long.parseLong(id));
            if (petitionType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionType>(petitionType, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionType>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionType> list = null;
        long count = 0;
        try {
            list = petitionTypeService.find(PetitionType.class, lazyEvent);
            count = petitionTypeService.count(PetitionType.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionType> dataTableDto = new DataTableDto<PetitionType>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionType>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionType>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionType> list = petitionTypeService.find(PetitionType.class, lazyEvent);
            
            List<PetitionType> activeList = list.stream()
                .filter(PetitionType::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionType>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition type (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionType petitionType = petitionTypeService.get(PetitionType.class, Long.parseLong(id));
            if (petitionType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionType.setActive(false);
            
            petitionType = petitionTypeService.modify(petitionType);
            
            MainDto dto = modelMapper.map(petitionType, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
