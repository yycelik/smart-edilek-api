package com.smart.edilek.controller.lookup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart.edilek.entity.lookup.PetitionPreferencesDateFormat;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lookup/petition-preferences-date-format")
@Tag(name = "Petition Preferences Date Format Lookup", description = "Petition preferences date format lookup endpoints")
public class PetitionPreferencesDateFormatController {

    @Autowired
    private GenericServiceImp<PetitionPreferencesDateFormat> petitionPreferencesDateFormatService;

    @GetMapping("/list")
    @Operation(summary = "Get all petition preferences date formats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionPreferencesDateFormat>> getAll() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<PetitionPreferencesDateFormat> list = petitionPreferencesDateFormatService.find(PetitionPreferencesDateFormat.class, lazyEvent);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
