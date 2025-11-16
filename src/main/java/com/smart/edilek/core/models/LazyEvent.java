package com.smart.edilek.core.models;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor 
public class LazyEvent {
    @Getter @Setter
    private int first;

    @Getter @Setter
    private int rows;

    @Getter @Setter
    private String sortField;

    @Getter @Setter
    private String sortOrder;

    @Getter @Setter
    private List<Object> multiSortMeta;

    @Getter @Setter
    private Map<String, FilterMeta> filters;

    @Getter @Setter
    private int page;

    @Getter @Setter
    private int pageCount;
}
