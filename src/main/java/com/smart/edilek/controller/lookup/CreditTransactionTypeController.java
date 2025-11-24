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

import com.smart.edilek.entity.lookup.CreditTransactionType;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/credit-transaction-type")
@Tag(name = "Credit Transaction Type Lookup", description = "Credit transaction type lookup endpoints")
public class CreditTransactionTypeController {

    @Autowired
    private GenericServiceImp<CreditTransactionType> creditTransactionTypeService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new credit transaction type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody CreditTransactionType creditTransactionType, Authentication authentication) {
        try {
            creditTransactionTypeService.add(creditTransactionType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(creditTransactionType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing credit transaction type", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody CreditTransactionType creditTransactionType, Authentication authentication) {
        try {
            creditTransactionType = creditTransactionTypeService.modify(creditTransactionType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(creditTransactionType, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get credit transaction type by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CreditTransactionType> get(@PathVariable String id) {
        CreditTransactionType creditTransactionType = null;
        try {
            creditTransactionType = creditTransactionTypeService.get(CreditTransactionType.class, Long.parseLong(id));
            if (creditTransactionType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<CreditTransactionType>(creditTransactionType, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of credit transaction types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<CreditTransactionType>> find(@RequestBody LazyEvent lazyEvent) {
        List<CreditTransactionType> list = null;
        long count = 0;
        try {
            list = creditTransactionTypeService.find(CreditTransactionType.class, lazyEvent);
            count = creditTransactionTypeService.count(CreditTransactionType.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<CreditTransactionType> dataTableDto = new DataTableDto<CreditTransactionType>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<CreditTransactionType>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active credit transaction types", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CreditTransactionType>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<CreditTransactionType> list = creditTransactionTypeService.find(CreditTransactionType.class, lazyEvent);
            
            List<CreditTransactionType> activeList = list.stream()
                .filter(CreditTransactionType::getActive)
                .toList();
            
            return new ResponseEntity<List<CreditTransactionType>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete credit transaction type (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            CreditTransactionType creditTransactionType = creditTransactionTypeService.get(CreditTransactionType.class, Long.parseLong(id));
            if (creditTransactionType == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            creditTransactionType.setActive(false);
            
            creditTransactionType = creditTransactionTypeService.modify(creditTransactionType);
            
            MainDto dto = modelMapper.map(creditTransactionType, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
