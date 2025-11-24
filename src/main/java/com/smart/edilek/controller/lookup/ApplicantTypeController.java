package com.smart.edilek.controller.lookup;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.lookup.ApplicantType;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/applicant-type")
@Tag(name = "Applicant Type Lookup", description = "Applicant type lookup endpoints")
public class ApplicantTypeController {

    @Autowired
    private GenericServiceImp<ApplicantType> applicantTypeService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new applicant type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody ApplicantType applicantType, Authentication authentication) {
        try {
            applicantTypeService.add(applicantType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(applicantType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing applicant type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody ApplicantType applicantType, Authentication authentication) {
        try {
            applicantType = applicantTypeService.modify(applicantType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(applicantType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get applicant type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApplicantType> get(@PathVariable String id) {
        ApplicantType applicantType = null;
        try {
            applicantType = applicantTypeService.get(ApplicantType.class, Long.parseLong(id));
            if (applicantType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<ApplicantType>(applicantType, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of applicant types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<ApplicantType>> find(@RequestBody LazyEvent lazyEvent) {
        List<ApplicantType> list = null;
        long count = 0;
        try {
            list = applicantTypeService.find(ApplicantType.class, lazyEvent);
            count = applicantTypeService.count(ApplicantType.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<ApplicantType> dataTableDto = new DataTableDto<ApplicantType>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<ApplicantType>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active applicant types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ApplicantType>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<ApplicantType> list = applicantTypeService.find(ApplicantType.class, lazyEvent);
            
            List<ApplicantType> activeList = list.stream()
                .filter(ApplicantType::getActive)
                .toList();
            
            return new ResponseEntity<List<ApplicantType>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete applicant type (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            ApplicantType applicantType = applicantTypeService.get(ApplicantType.class, Long.parseLong(id));
            if (applicantType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            applicantType.setActive(false);
            
            applicantType = applicantTypeService.modify(applicantType);
            
            MainDto dto = modelMapper.map(applicantType, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
