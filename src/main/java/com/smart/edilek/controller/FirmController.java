package com.smart.edilek.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.Firm;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.FirmDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/firm")
@Tag(name = "Firm Controller", description = "Firm management endpoints")
public class FirmController {

    @Autowired
    private GenericServiceImp<Firm> firmGenericService;

    @Autowired
    private ModelMapper modelMapper;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new firm", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addFirm(@RequestBody Firm firm) {
        try {
            firmGenericService.add(firm);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto firmDto = modelMapper.map(firm, MainDto.class);
        return new ResponseEntity<MainDto>(firmDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing firm", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyFirm(@RequestBody Firm firm) {
        try {
            firm = firmGenericService.modify(firm);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto firmDto = modelMapper.map(firm, MainDto.class);
        return new ResponseEntity<MainDto>(firmDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get firm by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<FirmDto> getFirm(@PathVariable long id) {
        Firm firm = null;
        try {
            firm = firmGenericService.get(Firm.class, id);
            if (firm == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        FirmDto firmDto = modelMapper.map(firm, FirmDto.class);
        return new ResponseEntity<FirmDto>(firmDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of firms", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<FirmDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<Firm> firmList = null;
        long count = 0;
        try {
            firmList = firmGenericService.find(Firm.class, lazyEvent);
            count = firmGenericService.count(Firm.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<FirmDto> dataTableDto = new DataTableDto<FirmDto>();
        List<FirmDto> firmDto = modelMapper.map(firmList, new TypeToken<List<FirmDto>>() {}.getType());
        dataTableDto.setData(firmDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<FirmDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active firms", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<FirmDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<Firm> firmList = firmGenericService.find(Firm.class, lazyEvent);
            
            List<Firm> activeList = firmList.stream()
                .filter(Firm::isActive)
                .toList();
            
            List<FirmDto> firmDto = modelMapper.map(activeList, new TypeToken<List<FirmDto>>() {}.getType());
            
            return new ResponseEntity<List<FirmDto>>(firmDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}