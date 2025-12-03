package com.smart.edilek.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.smart.edilek.entity.PetitionAgreements;
import com.smart.edilek.entity.PetitionAttachment;
import com.smart.edilek.entity.PetitionIdentity;
import com.smart.edilek.entity.PetitionPreferences;
import com.smart.edilek.entity.PetitionRequest;
import com.smart.edilek.entity.PetitionTypeInfo;
import com.smart.edilek.models.PetitionAgreementsDto;
import com.smart.edilek.models.PetitionAttachmentDto;
import com.smart.edilek.models.PetitionIdentityDto;
import com.smart.edilek.models.PetitionPreferencesDto;
import com.smart.edilek.models.PetitionRequestDto;
import com.smart.edilek.models.PetitionTypeInfoDto;

@Configuration
public class AppConfig {
	
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.typeMap(PetitionTypeInfo.class, PetitionTypeInfoDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getPetition().getId(), PetitionTypeInfoDto::setPetitionId));

        modelMapper.typeMap(PetitionRequest.class, PetitionRequestDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getPetition().getId(), PetitionRequestDto::setPetitionId));

        modelMapper.typeMap(PetitionAttachment.class, PetitionAttachmentDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getPetition().getId(), PetitionAttachmentDto::setPetitionId));

        modelMapper.typeMap(PetitionPreferences.class, PetitionPreferencesDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getPetition().getId(), PetitionPreferencesDto::setPetitionId));

        modelMapper.typeMap(PetitionIdentity.class, PetitionIdentityDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getPetition().getId(), PetitionIdentityDto::setPetitionId));

        modelMapper.typeMap(PetitionAgreements.class, PetitionAgreementsDto.class)
            .addMappings(mapper -> mapper.map(src -> src.getPetition().getId(), PetitionAgreementsDto::setPetitionId));

        return modelMapper;
    }
}
