package com.smart.edilek.controller;

import com.smart.edilek.core.annotation.LogExecutionTime;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smart.edilek.entity.PetitionAiHistory;
import com.smart.edilek.entity.Petition;
import com.smart.edilek.entity.User;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.LazyEvent;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.models.PetitionAiHistoryDto;
import com.smart.edilek.core.service.GenericServiceImp;

import io.swagger.v3.oas.annotations.tags.Tag;

@LogExecutionTime
@RestController
@RequestMapping("/petition-ai-history")
@Tag(name = "Petition AI History Controller", description = "Petition AI History management endpoints")
public class PetitionAiHistoryController {

    @Autowired
    private GenericServiceImp<PetitionAiHistory> petitionAiHistoryGenericService;

    @Autowired
    private GenericServiceImp<Petition> petitionGenericService;

    @Autowired
    private GenericServiceImp<User> userGenericService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/petition/{petitionId}")
    public List<PetitionAiHistoryDto> getHistoryByPetitionId(@PathVariable Long petitionId, Authentication authentication) {
        // Security Check
        Petition petition = petitionGenericService.get(Petition.class, petitionId);
        if (petition == null) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Petition not found");
        }

        String currentUserId = authentication.getName();
        User currentUser = userGenericService.get(User.class, currentUserId);

        boolean isOwner = petition.getUser().getId().equals(currentUser.getId());
        boolean isSameCompany = false;
        if (currentUser.getCompany() != null && petition.getUser().getCompany() != null) {
            isSameCompany = currentUser.getCompany().getId().equals(petition.getUser().getCompany().getId());
        }

        if (!isOwner && !isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this petition history");
        }

        LazyEvent lazyEvent = new LazyEvent();
        lazyEvent.setFilters(new HashMap<>());
        
        FilterMeta filterMeta = new FilterMeta();
        filterMeta.setOperator("and");
        
        Constraint constraint = new Constraint();
        constraint.setValue(petitionId);
        constraint.setMatchMode(MatchMode.equals.name());
        
        filterMeta.setConstraints(List.of(constraint));
        
        lazyEvent.getFilters().put("petition.id", filterMeta);
        
        List<PetitionAiHistory> historyList = petitionAiHistoryGenericService.find(PetitionAiHistory.class, lazyEvent);
        
        return historyList.stream()
                .map(history -> modelMapper.map(history, PetitionAiHistoryDto.class))
                .collect(Collectors.toList());
    }
}
