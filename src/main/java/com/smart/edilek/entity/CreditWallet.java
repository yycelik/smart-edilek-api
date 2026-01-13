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
@Table(name="credit_wallet")
@NamedQuery(name="CreditWallet.findAll", query="SELECT c FROM CreditWallet c")
public class CreditWallet implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@OneToOne
	@JoinColumn(name="user_id", nullable = true)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="company_id")
	private Company company;

	@Column(name="total_credits", nullable = false, columnDefinition = "INT DEFAULT 0")
	private Integer totalCredits = 0;

	@Column(name="used_credits", nullable = false, columnDefinition = "INT DEFAULT 0")
	private Integer usedCredits = 0;

	@Column(name="remaining_credits", nullable = false, columnDefinition = "INT DEFAULT 0")
	private Integer remainingCredits = 0;

	@Column(name="last_calculated_at")
	private LocalDateTime lastCalculatedAt;

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

	//bi-directional one-to-many association to CreditTransaction
	@OneToMany(mappedBy="wallet")
	private List<CreditTransaction> creditTransactions;

	@PrePersist
	protected void onCreate() {
		createdDate = LocalDateTime.now();
		if (updatedDate == null) {
			updatedDate = LocalDateTime.now();
		}
		lastCalculatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedDate = LocalDateTime.now();
		lastCalculatedAt = LocalDateTime.now();
	}
}
