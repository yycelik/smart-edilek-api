package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@Table(name="credit_transaction")
@NamedQuery(name="CreditTransaction.findAll", query="SELECT c FROM CreditTransaction c")
public class CreditTransaction implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name="wallet_id", nullable = false)
	private CreditWallet wallet;

	@ManyToOne
	@JoinColumn(name="user_order_id")
	private UserOrder userOrder;

	@ManyToOne
	@JoinColumn(name="petition_id")
	private Petition petition;

	@Column(name="type", nullable = false, length = 50)
	private String type;

	@Column(name="amount", nullable = false)
	private Integer amount;

	@Column(name="balance_after")
	private Integer balanceAfter;

	@Column(name="note", length = 1000)
	private String note;

	@Column(name="created_by", length = 255)
	private String createdBy;

	@Column(name="created_date")
	private LocalDateTime createdDate;

	@Column(name="updated_by", length = 255)
	private String updatedBy;

	@Column(name="updated_date")
	private LocalDateTime updatedDate;

	@Column(name="active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean active = true;

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