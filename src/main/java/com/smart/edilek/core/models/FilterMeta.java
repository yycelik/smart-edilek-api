package com.smart.edilek.core.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor 
public class FilterMeta {
    @Getter @Setter
    String operator;

    @Getter @Setter
    List<Constraint> constraints;
}
