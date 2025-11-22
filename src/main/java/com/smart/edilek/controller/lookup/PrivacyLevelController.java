package com.smart.edilek.controller.lookup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.lookup.PrivacyLevel;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/privacy-level")
@Tag(name = "Privacy Level Lookup", description = "Privacy level lookup endpoints")
public class PrivacyLevelController {

    @Autowired
    private GenericServiceImp<PrivacyLevel> privacyLevelService;

    @GetMapping("/list")
    @Operation(summary = "Get all privacy levels", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PrivacyLevel>> getAll() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PrivacyLevel> list = privacyLevelService.find(PrivacyLevel.class, lazyEvent);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
