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
@Table(name="petition_agreements")
@NamedQuery(name="PetitionAgreements.findAll", query="SELECT p FROM PetitionAgreements p")
public class PetitionAgreements implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@OneToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@Column(name="kvkk", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean kvkk = false;

	@Column(name="terms_of_service", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean termsOfService = false;

	@Column(name="data_sharing", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean dataSharing = false;

	@Column(name="institution_data_sharing", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean institutionDataSharing = false;

	@Column(name="signature_type", length = 20)
	private String signatureType;

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
