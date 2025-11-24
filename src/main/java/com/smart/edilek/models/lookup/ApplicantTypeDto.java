package com.smart.edilek.models.lookup;

import lombok.Getter;
import lombok.Setter;

public class ApplicantTypeDto {
	
	@Getter @Setter
	private long id;
	
	@Getter @Setter
	private String code;
	
	@Getter @Setter
	private String name;
	
	@Getter @Setter
	private String description;
	
	@Getter @Setter
	private boolean active;
	
}