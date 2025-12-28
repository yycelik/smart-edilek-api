package com.smart.edilek.models;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

public class PetitionAiHistoryDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String aiResult;

	@Getter @Setter
	private LocalDateTime createdDate;
}
