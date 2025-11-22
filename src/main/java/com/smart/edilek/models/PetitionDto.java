package com.smart.edilek.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class PetitionDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private UserDto user;

	@Getter @Setter
	private String petitionMode;

	@Getter @Setter
	private String status;

	@Getter @Setter
	private Integer creditCost;

	@Getter @Setter
	private String summaryTitle;

	@Getter @Setter
	private String summaryPetitionType;

	@Getter @Setter
	private String summaryInstitutionName;

	@Getter @Setter
	private PetitionAgreementsDto petitionAgreements;

	@Getter @Setter
	private PetitionTypeInfoDto petitionTypeInfo;

	@Getter @Setter
	private PetitionContentDto petitionContent;

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