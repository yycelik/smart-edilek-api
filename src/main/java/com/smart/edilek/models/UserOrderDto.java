package com.smart.edilek.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class UserOrderDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private UserDto user;

	@Getter @Setter
	private LicensePackageDto licensePackage;

	@Getter @Setter
	private String status;

	@Getter @Setter
	private BigDecimal price;

	@Getter @Setter
	private String currency;

	@Getter @Setter
	private String paymentProvider;

	@Getter @Setter
	private String paymentRef;

	@Getter @Setter
	private LocalDateTime purchasedAt;

	@Getter @Setter
	private LocalDateTime expiresAt;

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