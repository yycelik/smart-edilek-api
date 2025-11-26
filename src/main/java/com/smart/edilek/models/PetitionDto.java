package com.smart.edilek.models;

import java.time.LocalDateTime;
import java.util.List;

import com.smart.edilek.models.lookup.InstitutionCategoryDto;
import com.smart.edilek.models.lookup.PetitionModeDto;
import com.smart.edilek.models.lookup.PetitionStatusDto;
import com.smart.edilek.models.lookup.PetitionTypeDto;

import lombok.Getter;
import lombok.Setter;

public class PetitionDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private UserDto user;

	@Getter @Setter
	private PetitionModeDto petitionMode;

	@Getter @Setter
	private PetitionStatusDto petitionStatus;

	@Getter @Setter
	private Integer creditCost;

	@Getter @Setter
	private String title;

	@Getter @Setter
	private String summary;

	@Getter @Setter
	private PetitionTypeDto petitionType;

	@Getter @Setter
	private String typeName;

	@Getter @Setter
	private InstitutionCategoryDto institutionCategory;

	@Getter @Setter
	private String institutionName;

	@Getter @Setter
	private PetitionAgreementsDto petitionAgreements;

	@Getter @Setter
	private PetitionTypeInfoDto petitionTypeInfo;

	@Getter @Setter
	private List<PetitionRequestDto> petitionRequests;

	@Getter @Setter
	private List<PetitionAttachmentDto> petitionAttachments;

	@Getter @Setter
	private PetitionPreferencesDto petitionPreferences;

	@Getter @Setter
	private PetitionIdentityDto petitionIdentity;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}