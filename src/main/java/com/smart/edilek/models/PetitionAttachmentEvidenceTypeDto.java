package com.smart.edilek.models;

import java.time.LocalDateTime;

import com.smart.edilek.models.lookup.EvidenceTypeDto;

import lombok.Getter;
import lombok.Setter;

public class PetitionAttachmentEvidenceTypeDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionAttachmentId;

	@Getter @Setter
	private EvidenceTypeDto evidenceType;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}
