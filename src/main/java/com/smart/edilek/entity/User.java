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
@Table(name="user")
@NamedQuery(name="User.findAll", query="SELECT u FROM User u")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@Column(name="username", nullable = false, length = 255)
	private String username;

	@Column(name="firstname", nullable = false, length = 255)
	private String firstname;

	@Column(name="lastname", nullable = false, length = 255)
	private String lastname;

	@Column(name="email", nullable = false, length = 512)
	private String email;

	@Column(name="created_date")
	private LocalDateTime createdDate;

	@Column(name="updated_date")
	private LocalDateTime updatedDate;

	@Column(name="active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean active = true;

	//bi-directional one-to-many association to UserOrder
	@OneToMany(mappedBy="user")
	private List<UserOrder> userOrders;

	//bi-directional one-to-one association to CreditWallet
	@OneToOne(mappedBy="user")
	private CreditWallet creditWallet;

	//bi-directional one-to-many association to Petition
	@OneToMany(mappedBy="user")
	private List<Petition> petitions;

	//bi-directional one-to-many association to CreditTransaction
	@OneToMany(mappedBy="user")
	private List<CreditTransaction> creditTransactions;

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
