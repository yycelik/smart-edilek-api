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

import com.smart.edilek.entity.User;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.UserDto;
import com.smart.edilek.security.jwt.KeycloakJwtUtils;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "User management endpoints")
public class UserController {

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeycloakJwtUtils keycloakJwtUtils;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addUser(@RequestBody User user, Authentication authentication) {
        try {
            userGenericService.add(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto userDto = modelMapper.map(user, MainDto.class);
        return new ResponseEntity<MainDto>(userDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyUser(@RequestBody User user, Authentication authentication) {
        try {
            user = userGenericService.modify(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto userDto = modelMapper.map(user, MainDto.class);
        return new ResponseEntity<MainDto>(userDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        User user = null;
        try {
            user = userGenericService.get(User.class, Long.parseLong(id));
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        UserDto userDto = modelMapper.map(user, UserDto.class);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
    }
    
    @GetMapping("/list/{lazyEvent}")
    @Operation(summary = "Get paginated list of users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<UserDto>> find(@PathVariable("lazyEvent") LazyEvent lazyEvent) {
        List<User> userList = null;
        long count = 0;
        try {
            userList = userGenericService.find(User.class, lazyEvent);
            count = userGenericService.count(User.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<UserDto> dataTableDto = new DataTableDto<UserDto>();
        List<UserDto> userDto = modelMapper.map(userList, new TypeToken<List<UserDto>>() {}.getType());
        dataTableDto.setData(userDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<UserDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<User> userList = userGenericService.find(User.class, lazyEvent);
            
            List<User> activeList = userList.stream()
                .filter(User::getActive)
                .toList();
            
            List<UserDto> userDto = modelMapper.map(activeList, new TypeToken<List<UserDto>>() {}.getType());
            
            return new ResponseEntity<List<UserDto>>(userDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete user (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deleteUser(@PathVariable String id, Authentication authentication) {
        try {
            User user = userGenericService.get(User.class, Long.parseLong(id));
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            user.setActive(false);
            
            user = userGenericService.modify(user);
            
            MainDto userDto = modelMapper.map(user, MainDto.class);
            return new ResponseEntity<MainDto>(userDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}