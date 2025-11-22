package com.smart.edilek.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class LicensePackageDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String code;

	@Getter @Setter
	private String name;

	@Getter @Setter
	private String description;

	@Getter @Setter
	private BigDecimal price;

	@Getter @Setter
	private String currency;

	@Getter @Setter
	private Integer creditAmount;

	@Getter @Setter
	private Boolean isOneTime;

	@Getter @Setter
	private Integer durationDays;

	@Getter @Setter
	private Integer sortOrder;

	@Getter @Setter
	private Boolean isActive;

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