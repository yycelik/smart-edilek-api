package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class PetitionPreferencesDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private String style;

	@Getter @Setter
	private String language;

	@Getter @Setter
	private String length;

	@Getter @Setter
	private Boolean paragraphs;

	@Getter @Setter
	private Boolean bulletPoints;

	@Getter @Setter
	private Boolean legalReferences;

	@Getter @Setter
	private String dateFormat;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}