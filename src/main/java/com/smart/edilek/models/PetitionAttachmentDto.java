package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class PetitionAttachmentDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private String fileName;

	@Getter @Setter
	private String filePath;

	@Getter @Setter
	private String fileType;

	@Getter @Setter
	private Long fileSizeLong;

	@Getter @Setter
	private String title;

	@Getter @Setter
	private String description;

	@Getter @Setter
	private LocalDateTime uploadDate;

	@Getter @Setter
	private String privacyLevel;

	@Getter @Setter
	private String evidenceTypes;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}