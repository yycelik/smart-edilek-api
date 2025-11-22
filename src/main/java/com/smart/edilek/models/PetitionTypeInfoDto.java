package com.smart.edilek.models;

import java.time.LocalDateTime;

import com.smart.edilek.models.lookup.InstitutionCategoryDto;
import com.smart.edilek.models.lookup.PetitionTypeDto;

import lombok.Getter;
import lombok.Setter;

public class PetitionTypeInfoDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private PetitionTypeDto petitionType;

	@Getter @Setter
	private String customTypeName;

	@Getter @Setter
	private InstitutionCategoryDto institutionCategory;

	@Getter @Setter
	private String institutionName;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}