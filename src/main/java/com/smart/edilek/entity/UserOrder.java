package com.smart.edilek.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.smart.edilek.entity.lookup.Currency;
import com.smart.edilek.entity.lookup.PaymentProvider;
import com.smart.edilek.entity.lookup.UserOrderStatus;

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
@Table(name="user_order")
@NamedQuery(name="UserOrder.findAll", query="SELECT u FROM UserOrder u")
public class UserOrder implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="company_id")
	private Company company;

	@ManyToOne
	@JoinColumn(name="license_package_id", nullable = false)
	private LicensePackage licensePackage;

	@ManyToOne
	@JoinColumn(name="user_order_status_id", nullable = false)
	private UserOrderStatus userOrderStatus;

	@ManyToOne
	@JoinColumn(name="payment_provider_id", nullable = false)
	private PaymentProvider paymentProvider;

	@Column(name="price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name="currency_id", nullable = false)
	private Currency currency;

	@Column(name="payment_ref", length = 255)
	private String paymentRef;

	@Column(name="purchased_at")
	private LocalDateTime purchasedAt;

	@Column(name="expires_at")
	private LocalDateTime expiresAt;

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
	@OneToMany(mappedBy="userOrder")
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
