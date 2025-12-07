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

import com.smart.edilek.entity.lookup.EvidenceType;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/lookup/evidence-type")
@Tag(name = "Evidence Type Lookup", description = "Evidence type lookup endpoints")
public class EvidenceTypeController {

    @Autowired
    private GenericServiceImp<EvidenceType> evidenceTypeService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new evidence type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody EvidenceType evidenceType, Authentication authentication) {
        try {
            evidenceTypeService.add(evidenceType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(evidenceType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing evidence type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody EvidenceType evidenceType, Authentication authentication) {
        try {
            evidenceType = evidenceTypeService.modify(evidenceType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(evidenceType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get evidence type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EvidenceType> get(@PathVariable String id) {
        EvidenceType evidenceType = null;
        try {
            evidenceType = evidenceTypeService.get(EvidenceType.class, Long.parseLong(id));
            if (evidenceType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<EvidenceType>(evidenceType, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of evidence types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<EvidenceType>> find(@RequestBody LazyEvent lazyEvent) {
        List<EvidenceType> list = null;
        long count = 0;
        try {
            list = evidenceTypeService.find(EvidenceType.class, lazyEvent);
            count = evidenceTypeService.count(EvidenceType.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<EvidenceType> dataTableDto = new DataTableDto<EvidenceType>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<EvidenceType>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active evidence types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<EvidenceType>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<EvidenceType> list = evidenceTypeService.find(EvidenceType.class, lazyEvent);
            
            List<EvidenceType> activeList = list.stream()
                .filter(EvidenceType::getActive)
                .toList();
            
            return new ResponseEntity<List<EvidenceType>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete evidence type (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            EvidenceType evidenceType = evidenceTypeService.get(EvidenceType.class, Long.parseLong(id));
            if (evidenceType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            evidenceType.setActive(false);
            
            evidenceType = evidenceTypeService.modify(evidenceType);
            
            MainDto dto = modelMapper.map(evidenceType, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
