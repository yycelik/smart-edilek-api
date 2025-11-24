package com.smart.edilek.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.smart.edilek.entity.lookup.Currency;

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
@Table(name="license_package")
@NamedQuery(name="LicensePackage.findAll", query="SELECT l FROM LicensePackage l")
public class LicensePackage implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(name="code", nullable = false, length = 50)
	private String code;

	@Column(name="name", nullable = false, length = 255)
	private String name;

	@Column(name="description", nullable = false, length = 4000)
	private String description;

	@Column(name="price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name="currency_id", nullable = false)
	private Currency currency;

	@Column(name="credit_amount", nullable = false)
	private Integer creditAmount;

	@Column(name="is_one_time", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean isOneTime = true;

	@Column(name="duration_days")
	private Integer durationDays;

	@Column(name="sort_order")
	private Integer sortOrder;

	@Column(name="is_active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean isActive = true;

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

	//bi-directional one-to-many association to UserOrder
	@OneToMany(mappedBy="licensePackage")
	private List<UserOrder> userOrders;

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
