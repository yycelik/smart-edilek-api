package com.smart.edilek.models;

import java.time.LocalDateTime;

import com.smart.edilek.models.lookup.PetitionPreferencesDateFormatDto;
import com.smart.edilek.models.lookup.PetitionPreferencesLanguageDto;
import com.smart.edilek.models.lookup.PetitionPreferencesLengthDto;
import com.smart.edilek.models.lookup.PetitionPreferencesStyleDto;

import lombok.Getter;
import lombok.Setter;

public class PetitionPreferencesDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private PetitionPreferencesStyleDto style;

	@Getter @Setter
	private PetitionPreferencesLanguageDto language;

	@Getter @Setter
	private PetitionPreferencesLengthDto length;

	@Getter @Setter
	private Boolean paragraphs;

	@Getter @Setter
	private Boolean bulletPoints;

	@Getter @Setter
	private Boolean legalReferences;

	@Getter @Setter
	private PetitionPreferencesDateFormatDto dateFormat;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}