package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class PetitionTypeInfoDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private String type;

	@Getter @Setter
	private String customTypeName;

	@Getter @Setter
	private String institutionCategory;

	@Getter @Setter
	private String institutionName;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}