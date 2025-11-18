package com.smart.edilek.models;

import lombok.Getter;
import lombok.Setter;

public class TypeDto {

	@Getter @Setter
	private long id;

	@Getter @Setter
	private String name;

	@Getter @Setter
	private boolean active;
}