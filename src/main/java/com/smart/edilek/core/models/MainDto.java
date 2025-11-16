package com.smart.edilek.core.models;

import lombok.Getter;
import lombok.Setter;

public class MainDto {

	@Getter @Setter
	private int id;

	@Getter @Setter
	private boolean active = true;

	@Getter @Setter
	private String name;	
}