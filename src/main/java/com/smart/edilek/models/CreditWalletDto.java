package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class CreditWalletDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private UserDto user;

	@Getter @Setter
	private CompanyDto company;

	@Getter @Setter
	private Integer totalCredits;

	@Getter @Setter
	private Integer usedCredits;

	@Getter @Setter
	private Integer remainingCredits;

	@Getter @Setter
	private LocalDateTime lastCalculatedAt;

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