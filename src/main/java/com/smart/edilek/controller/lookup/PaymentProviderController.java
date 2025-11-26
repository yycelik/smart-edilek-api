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

import com.smart.edilek.entity.lookup.PaymentProvider;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/payment-provider")
@Tag(name = "Payment Provider Lookup", description = "Payment provider lookup endpoints")
public class PaymentProviderController {

    @Autowired
    private GenericServiceImp<PaymentProvider> paymentProviderService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping(value = "/add")
    @Operation(summary = "Add new payment provider", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> add(@RequestBody PaymentProvider paymentProvider, Authentication authentication) {
        try {
            paymentProviderService.add(paymentProvider);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(paymentProvider, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing payment provider", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> modify(@RequestBody PaymentProvider paymentProvider, Authentication authentication) {
        try {
            paymentProvider = paymentProviderService.modify(paymentProvider);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto dto = modelMapper.map(paymentProvider, MainDto.class);
        return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get payment provider by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PaymentProvider> get(@PathVariable String id) {
        PaymentProvider paymentProvider = null;
        try {
            paymentProvider = paymentProviderService.get(PaymentProvider.class, Long.parseLong(id));
            if (paymentProvider == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return new ResponseEntity<PaymentProvider>(paymentProvider, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of payment providers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PaymentProvider>> find(@RequestBody LazyEvent lazyEvent) {
        List<PaymentProvider> list = null;
        long count = 0;
        try {
            list = paymentProviderService.find(PaymentProvider.class, lazyEvent);
            count = paymentProviderService.count(PaymentProvider.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PaymentProvider> dataTableDto = new DataTableDto<PaymentProvider>();
        dataTableDto.setData(list);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PaymentProvider>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active payment providers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PaymentProvider>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PaymentProvider> list = paymentProviderService.find(PaymentProvider.class, lazyEvent);
            
            List<PaymentProvider> activeList = list.stream()
                .filter(PaymentProvider::getActive)
                .toList();
            
            return new ResponseEntity<List<PaymentProvider>>(activeList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete payment provider (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainDto> delete(@PathVariable String id, Authentication authentication) {
        try {
            PaymentProvider paymentProvider = paymentProviderService.get(PaymentProvider.class, Long.parseLong(id));
            if (paymentProvider == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            paymentProvider.setActive(false);
            
            paymentProvider = paymentProviderService.modify(paymentProvider);
            
            MainDto dto = modelMapper.map(paymentProvider, MainDto.class);
            return new ResponseEntity<MainDto>(dto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
