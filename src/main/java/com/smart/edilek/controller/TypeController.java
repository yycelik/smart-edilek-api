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

import com.smart.edilek.entity.Type;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.TypeDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/type")
@Tag(name = "Type Controller", description = "Type management endpoints")
public class TypeController {

    @Autowired
    private GenericServiceImp<Type> typeGenericService;

    @Autowired
    private ModelMapper modelMapper;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addType(@RequestBody Type type) {
        try {
            typeGenericService.add(type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto typeDto = modelMapper.map(type, MainDto.class);
        return new ResponseEntity<MainDto>(typeDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyType(@RequestBody Type type) {
        try {
            type = typeGenericService.modify(type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto typeDto = modelMapper.map(type, MainDto.class);
        return new ResponseEntity<MainDto>(typeDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TypeDto> getType(@PathVariable long id) {
        Type type = null;
        try {
            type = typeGenericService.get(Type.class, id);
            if (type == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        TypeDto typeDto = modelMapper.map(type, TypeDto.class);
        return new ResponseEntity<TypeDto>(typeDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<TypeDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<Type> typeList = null;
        long count = 0;
        try {
            typeList = typeGenericService.find(Type.class, lazyEvent);
            count = typeGenericService.count(Type.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<TypeDto> dataTableDto = new DataTableDto<TypeDto>();
        List<TypeDto> typeDto = modelMapper.map(typeList, new TypeToken<List<TypeDto>>() {}.getType());
        dataTableDto.setData(typeDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<TypeDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TypeDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<Type> typeList = typeGenericService.find(Type.class, lazyEvent);
            
            List<Type> activeList = typeList.stream()
                .filter(Type::isActive)
                .toList();
            
            List<TypeDto> typeDto = modelMapper.map(activeList, new TypeToken<List<TypeDto>>() {}.getType());
            
            return new ResponseEntity<List<TypeDto>>(typeDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}