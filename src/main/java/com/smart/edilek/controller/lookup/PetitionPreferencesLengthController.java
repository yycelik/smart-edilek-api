package com.smart.edilek.controller.lookup;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;

import org.modelmapper.ModelMapper;
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

import com.smart.edilek.entity.lookup.PetitionPreferencesLength;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/lookup/petition-preferences-length")
@Tag(name = "Petition Preferences Length Lookup", description = "Petition preferences length lookup endpoints")
public class PetitionPreferencesLengthController {

    @Autowired
    private GenericServiceImp<PetitionPreferencesLength> petitionPreferencesLengthService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition preferences length", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionPreferencesLength petitionPreferencesLength, Authentication authentication) {
        try {
            petitionPreferencesLengthService.add(petitionPreferencesLength);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesLength, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition preferences length", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionPreferencesLength petitionPreferencesLength, Authentication authentication) {
        try {
            petitionPreferencesLength = petitionPreferencesLengthService.modify(petitionPreferencesLength);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesLength, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition preferences length by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionPreferencesLength> get(@PathVariable String id) {
        PetitionPreferencesLength petitionPreferencesLength = null;
        try {
            petitionPreferencesLength = petitionPreferencesLengthService.get(PetitionPreferencesLength.class, Long.parseLong(id));
            if (petitionPreferencesLength == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionPreferencesLength>(petitionPreferencesLength, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition preferences lengths", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionPreferencesLength>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionPreferencesLength> list = null;
        long count = 0;
        try {
            list = petitionPreferencesLengthService.find(PetitionPreferencesLength.class, lazyEvent);
            count = petitionPreferencesLengthService.count(PetitionPreferencesLength.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionPreferencesLength> dataTableDto = new DataTableDto<PetitionPreferencesLength>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionPreferencesLength>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition preferences lengths", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionPreferencesLength>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionPreferencesLength> list = petitionPreferencesLengthService.find(PetitionPreferencesLength.class, lazyEvent);
            
            List<PetitionPreferencesLength> activeList = list.stream()
                .filter(PetitionPreferencesLength::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionPreferencesLength>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition preferences length (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionPreferencesLength petitionPreferencesLength = petitionPreferencesLengthService.get(PetitionPreferencesLength.class, Long.parseLong(id));
            if (petitionPreferencesLength == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionPreferencesLength.setActive(false);
            
            petitionPreferencesLength = petitionPreferencesLengthService.modify(petitionPreferencesLength);
            
            MainDto dto = modelMapper.map(petitionPreferencesLength, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
