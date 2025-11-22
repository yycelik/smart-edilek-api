package com.smart.edilek.models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

public class UserDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String username;

	@Getter @Setter
	private String firstname;

	@Getter @Setter
	private String lastname;

	@Getter @Setter
	private String email;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}