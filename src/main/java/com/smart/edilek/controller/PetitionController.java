package com.smart.edilek.controller;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;
import java.util.HashMap;

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

import com.smart.edilek.entity.Petition;
import com.smart.edilek.entity.PetitionAiHistory;
import com.smart.edilek.entity.User;
import com.smart.edilek.entity.Company;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.DataTableDto;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.models.MainDto;
import com.smart.edilek.models.PetitionDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/petition")
@Tag(name = "Petition Controller", description = "Petition management endpoints")
public class PetitionController {

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GenericServiceImp<PetitionAiHistory> petitionAiHistoryGenericService;
    
    @PostMapping(value = "/add")
    @Operation(summary = "Add new petition", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> addPetition(@RequestBody Petition petition, Authentication authentication) {
        try {
            // Set relationships
            if (petition.getUser() != null && petition.getUser().getId() != null && !petition.getUser().getId().isEmpty()) {
                User user = userGenericService.get(User.class, petition.getUser().getId());
                petition.setUser(user);
                
                if (user.getCompany() != null) {
                    petition.setCompany(user.getCompany());
                }
            }

            petitionGenericService.add(petition);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionDto = new MainDto();
        petitionDto.setId((int) petition.getId());
        petitionDto.setActive(petition.getActive());
        petitionDto.setName(petition.getTitle());
        
        return new ResponseEntity<MainDto>(petitionDto, HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/modify")
    @Operation(summary = "Modify existing petition", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> modifyPetition(@RequestBody Petition petition, Authentication authentication) {
        try {
            // Set relationships
            if (petition.getUser() != null && petition.getUser().getId() != null && !petition.getUser().getId().isEmpty()) {
                User user = userGenericService.get(User.class, petition.getUser().getId());
                petition.setUser(user);
                
                if (user.getCompany() != null) {
                    petition.setCompany(user.getCompany());
                }
            }

            petition = petitionGenericService.modify(petition);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        MainDto petitionDto = new MainDto();
        petitionDto.setId((int) petition.getId());
        petitionDto.setActive(petition.getActive());
        petitionDto.setName(petition.getTitle());
        
        return new ResponseEntity<MainDto>(petitionDto, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    @Operation(summary = "Get petition by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PetitionDto> getPetition(@PathVariable String id) {
        Petition petition = null;
        try {
            petition = petitionGenericService.get(Petition.class, Long.parseLong(id));
            if (petition == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        PetitionDto petitionDto = modelMapper.map(petition, PetitionDto.class);
        return new ResponseEntity<PetitionDto>(petitionDto, HttpStatus.OK);
    }
    
    @PostMapping("/list")
    @Operation(summary = "Get paginated list of petitions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DataTableDto<PetitionDto>> find(@RequestBody LazyEvent lazyEvent, Authentication authentication) {
        List<Petition> petitionList = null;
        long count = 0;
        try {
            if (authentication != null && authentication.getName() != null) {
                if (lazyEvent.getFilters() == null) {
                    lazyEvent.setFilters(new HashMap<>());
                }
                
                FilterMeta filterMeta = new FilterMeta();
                filterMeta.setOperator("and");
                
                Constraint constraint = new Constraint();
                constraint.setMatchMode("equals");
                
                User currentUser = userGenericService.get(User.class, authentication.getName());
                
                if (currentUser != null && currentUser.getCompany() != null) {
                    constraint.setValue(currentUser.getCompany().getId().toString());
                    filterMeta.setConstraints(List.of(constraint));
                    lazyEvent.getFilters().put("company.id", filterMeta);
                } else {
                    constraint.setValue(authentication.getName());
                    filterMeta.setConstraints(List.of(constraint));
                    lazyEvent.getFilters().put("user.id", filterMeta);
                }
            }

            petitionList = petitionGenericService.find(Petition.class, lazyEvent);
            count = petitionGenericService.count(Petition.class, lazyEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DataTableDto<PetitionDto> dataTableDto = new DataTableDto<PetitionDto>();
        List<PetitionDto> petitionDto = modelMapper.map(petitionList, new TypeToken<List<PetitionDto>>() {}.getType());
        dataTableDto.setData(petitionDto);
        dataTableDto.setTotalRecords(count);

        return new ResponseEntity<DataTableDto<PetitionDto>>(dataTableDto, HttpStatus.OK);
    }

    @GetMapping("/list/all")
    @Operation(summary = "Get all active petitions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PetitionDto>> getAllActive() {
        try {
            LazyEvent lazyEvent = new LazyEvent();
            lazyEvent.setFirst(0);
            lazyEvent.setRows(1000);
            lazyEvent.setPage(0);
            
            List<Petition> petitionList = petitionGenericService.find(Petition.class, lazyEvent);
            
            List<Petition> activeList = petitionList.stream()
                .filter(Petition::getActive)
                .toList();
            
            List<PetitionDto> petitionDto = modelMapper.map(activeList, new TypeToken<List<PetitionDto>>() {}.getType());
            
            return new ResponseEntity<List<PetitionDto>>(petitionDto, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Soft delete petition (set active to false)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MainDto> deletePetition(@PathVariable String id, Authentication authentication) {
        try {
            Petition petition = petitionGenericService.get(Petition.class, Long.parseLong(id));
            if (petition == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Soft delete - just set active to false
            petition.setActive(false);
            
            petition = petitionGenericService.modify(petition);
            
            MainDto petitionDto = new MainDto();
            petitionDto.setId((int) petition.getId());
            petitionDto.setActive(petition.getActive());
            petitionDto.setName(petition.getTitle());
            
            return new ResponseEntity<MainDto>(petitionDto, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/ai-result")
    public ResponseEntity<PetitionDto> updateAiResult(@PathVariable Long id, @RequestBody String aiResult) {
        Petition petition = petitionGenericService.get(Petition.class, id);
        if (petition == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        petition.setAiResult(aiResult);
        petitionGenericService.modify(petition);

        PetitionAiHistory history = new PetitionAiHistory();
        history.setPetition(petition);
        history.setAiResult(aiResult);
        petitionAiHistoryGenericService.add(history);

        return new ResponseEntity<>(modelMapper.map(petition, PetitionDto.class), HttpStatus.OK);
    }
}