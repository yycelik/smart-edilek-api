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

import com.smart.edilek.entity.lookup.PetitionMode;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/petition-mode")
@Tag(name = "Petition Mode Lookup", description = "Petition mode lookup endpoints")
public class PetitionModeController {

    @Autowired
    private GenericServiceImp<PetitionMode> petitionModeService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition mode", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionMode petitionMode, Authentication authentication) {
        try {
            petitionModeService.add(petitionMode);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionMode, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition mode", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionMode petitionMode, Authentication authentication) {
        try {
            petitionMode = petitionModeService.modify(petitionMode);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionMode, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition mode by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionMode> get(@PathVariable String id) {
        PetitionMode petitionMode = null;
        try {
            petitionMode = petitionModeService.get(PetitionMode.class, Long.parseLong(id));
            if (petitionMode == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionMode>(petitionMode, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition modes", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionMode>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionMode> list = null;
        long count = 0;
        try {
            list = petitionModeService.find(PetitionMode.class, lazyEvent);
            count = petitionModeService.count(PetitionMode.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionMode> dataTableDto = new DataTableDto<PetitionMode>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionMode>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition modes", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionMode>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionMode> list = petitionModeService.find(PetitionMode.class, lazyEvent);
            
            List<PetitionMode> activeList = list.stream()
                .filter(PetitionMode::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionMode>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition mode (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionMode petitionMode = petitionModeService.get(PetitionMode.class, Long.parseLong(id));
            if (petitionMode == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionMode.setActive(false);
            
            petitionMode = petitionModeService.modify(petitionMode);
            
            MainDto dto = modelMapper.map(petitionMode, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
