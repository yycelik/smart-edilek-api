package com.smart.edilek.core.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class DataTableDto<T> {

    @Getter @Setter
	private Long totalRecords;

    @Getter @Setter
	private List<T> data;
}