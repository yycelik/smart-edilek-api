package com.smart.edilek.core.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor 
public class Constraint {
    @Getter @Setter
    Object value;

    @Getter @Setter
    String matchMode;
}
