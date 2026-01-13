package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="company")
public class Company implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", unique = true, nullable = false)
    private Long id;

    @Column(name="name", nullable = false, length = 255)
    private String name;

    @Column(name="tax_id", length = 50)
    private String taxId;

    @Column(name="tax_office", length = 255)
    private String taxOffice;

    @Column(name="address", columnDefinition = "TEXT")
    private String address;

    @Column(name="email", length = 255)
    private String email;

    @Column(name="phone", length = 50)
    private String phone;

    @Column(name="subscription_status", length = 50)
    private String subscriptionStatus;

    @Column(name="created_date")
    private LocalDateTime createdDate;

    @Column(name="updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy="company")
    private List<User> users;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<CreditWallet> creditWallets;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (updatedDate == null) {
            updatedDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
