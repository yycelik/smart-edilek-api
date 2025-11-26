package com.smart.edilek.controller.lookup;

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

import com.smart.edilek.entity.lookup.PetitionPreferencesStyle;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/petition-preferences-style")
@Tag(name = "Petition Preferences Style Lookup", description = "Petition preferences style lookup endpoints")
public class PetitionPreferencesStyleController {

    @Autowired
    private GenericServiceImp<PetitionPreferencesStyle> petitionPreferencesStyleService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition preferences style", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PetitionPreferencesStyle petitionPreferencesStyle, Authentication authentication) {
        try {
            petitionPreferencesStyleService.add(petitionPreferencesStyle);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesStyle, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition preferences style", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PetitionPreferencesStyle petitionPreferencesStyle, Authentication authentication) {
        try {
            petitionPreferencesStyle = petitionPreferencesStyleService.modify(petitionPreferencesStyle);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(petitionPreferencesStyle, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition preferences style by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionPreferencesStyle> get(@PathVariable String id) {
        PetitionPreferencesStyle petitionPreferencesStyle = null;
        try {
            petitionPreferencesStyle = petitionPreferencesStyleService.get(PetitionPreferencesStyle.class, Long.parseLong(id));
            if (petitionPreferencesStyle == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PetitionPreferencesStyle>(petitionPreferencesStyle, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petition preferences styles", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionPreferencesStyle>> find(@RequestBody LazyEvent lazyEvent) {
        List<PetitionPreferencesStyle> list = null;
        long count = 0;
        try {
            list = petitionPreferencesStyleService.find(PetitionPreferencesStyle.class, lazyEvent);
            count = petitionPreferencesStyleService.count(PetitionPreferencesStyle.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionPreferencesStyle> dataTableDto = new DataTableDto<PetitionPreferencesStyle>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionPreferencesStyle>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petition preferences styles", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionPreferencesStyle>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionPreferencesStyle> list = petitionPreferencesStyleService.find(PetitionPreferencesStyle.class, lazyEvent);
            
            List<PetitionPreferencesStyle> activeList = list.stream()
                .filter(PetitionPreferencesStyle::getActive)
                .toList();
            
            return new ResponseEntity<List<PetitionPreferencesStyle>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition preferences style (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PetitionPreferencesStyle petitionPreferencesStyle = petitionPreferencesStyleService.get(PetitionPreferencesStyle.class, Long.parseLong(id));
            if (petitionPreferencesStyle == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petitionPreferencesStyle.setActive(false);
            
            petitionPreferencesStyle = petitionPreferencesStyleService.modify(petitionPreferencesStyle);
            
            MainDto dto = modelMapper.map(petitionPreferencesStyle, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
