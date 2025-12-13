package com.smart.edilek.models;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String taxId;
    private String taxOffice;
    private String address;
    private String email;
    private String phone;
    private String subscriptionStatus;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
