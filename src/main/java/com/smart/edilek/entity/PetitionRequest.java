package com.smart.edilek.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smart.edilek.entity.lookup.Currency;
import com.smart.edilek.entity.lookup.PetitionRequestType;

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
@Table(name="petition_request")
@NamedQuery(name="PetitionRequest.findAll", query="SELECT p FROM PetitionRequest p")
public class PetitionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@ManyToOne
	@JoinColumn(name="petition_request_type_id", nullable = false)
	private PetitionRequestType petitionRequestType;

	@Column(name="description", nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(name="amount", precision = 18, scale = 2)
	private BigDecimal amount;

	@Column(name="deadline")
	private LocalDateTime deadline;

	@ManyToOne
	@JoinColumn(name="currency_id")
	private Currency currency;

	@Column(name="created_date")
	private LocalDateTime createdDate;

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