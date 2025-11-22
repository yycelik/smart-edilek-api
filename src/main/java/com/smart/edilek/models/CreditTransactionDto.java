package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class CreditTransactionDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private UserDto user;

	@Getter @Setter
	private CreditWalletDto wallet;

	@Getter @Setter
	private UserOrderDto userOrder;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private String type;

	@Getter @Setter
	private Integer amount;

	@Getter @Setter
	private Integer balanceAfter;

	@Getter @Setter
	private String note;

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