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

import com.smart.edilek.entity.lookup.UserOrderStatus;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/user-order-status")
@Tag(name = "User Order Status Lookup", description = "User order status lookup endpoints")
public class UserOrderStatusController {

    @Autowired
    private GenericServiceImp<UserOrderStatus> userOrderStatusService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new user order status", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody UserOrderStatus userOrderStatus, Authentication authentication) {
        try {
            userOrderStatusService.add(userOrderStatus);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(userOrderStatus, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing user order status", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody UserOrderStatus userOrderStatus, Authentication authentication) {
        try {
            userOrderStatus = userOrderStatusService.modify(userOrderStatus);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(userOrderStatus, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get user order status by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserOrderStatus> get(@PathVariable String id) {
        UserOrderStatus userOrderStatus = null;
        try {
            userOrderStatus = userOrderStatusService.get(UserOrderStatus.class, Long.parseLong(id));
            if (userOrderStatus == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<UserOrderStatus>(userOrderStatus, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of user order statuses", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<UserOrderStatus>> find(@RequestBody LazyEvent lazyEvent) {
        List<UserOrderStatus> list = null;
        long count = 0;
        try {
            list = userOrderStatusService.find(UserOrderStatus.class, lazyEvent);
            count = userOrderStatusService.count(UserOrderStatus.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<UserOrderStatus> dataTableDto = new DataTableDto<UserOrderStatus>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<UserOrderStatus>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active user order statuses", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserOrderStatus>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<UserOrderStatus> list = userOrderStatusService.find(UserOrderStatus.class, lazyEvent);
            
            List<UserOrderStatus> activeList = list.stream()
                .filter(UserOrderStatus::getActive)
                .toList();
            
            return new ResponseEntity<List<UserOrderStatus>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete user order status (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            UserOrderStatus userOrderStatus = userOrderStatusService.get(UserOrderStatus.class, Long.parseLong(id));
            if (userOrderStatus == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            userOrderStatus.setActive(false);
            
            userOrderStatus = userOrderStatusService.modify(userOrderStatus);
            
            MainDto dto = modelMapper.map(userOrderStatus, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
