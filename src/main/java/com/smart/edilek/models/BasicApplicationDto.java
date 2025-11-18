package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class BasicApplicationDto {

	@Getter @Setter
	private long id;

	@Getter @Setter
	private TypeDto type;

	@Getter @Setter
	private String typeOther;

	@Getter @Setter
	private FirmDto firm;

	@Getter @Setter
	private String firmOther;

	@Getter @Setter
	private String title;

	@Getter @Setter
	private String description;

	@Getter @Setter
	private String createdBy;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private String updatedBy;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}