package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class PetitionContentDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private String title;

	@Getter @Setter
	private String summary;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}