package com.smart.edilek.models;

import java.time.LocalDateTime;

import com.smart.edilek.models.lookup.SignatureTypeDto;

import lombok.Getter;
import lombok.Setter;

public class PetitionAgreementsDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private Boolean kvkk;

	@Getter @Setter
	private Boolean termsOfService;

	@Getter @Setter
	private Boolean dataSharing;

	@Getter @Setter
	private Boolean institutionDataSharing;

	@Getter @Setter
	private SignatureTypeDto signatureType;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}