package com.smart.edilek.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class PetitionRequestDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private String type;

	@Getter @Setter
	private String description;

	@Getter @Setter
	private BigDecimal amount;

	@Getter @Setter
	private LocalDateTime deadline;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}