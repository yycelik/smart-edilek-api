package com.smart.edilek.controller.lookup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.lookup.PaymentProvider;
import com.smart.edilek.core.models.LazyEvent;
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

    @GetMapping("/list")
    @Operation(summary = "Get all payment providers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PaymentProvider>> getAll() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PaymentProvider> list = paymentProviderService.find(PaymentProvider.class, lazyEvent);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
