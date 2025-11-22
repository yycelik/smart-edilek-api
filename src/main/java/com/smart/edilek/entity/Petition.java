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
@Table(name="petition")
@NamedQuery(name="Petition.findAll", query="SELECT p FROM Petition p")
public class Petition implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private User user;

	@Column(name="petition_mode", nullable = false, length = 20)
	private String petitionMode;

	@Column(name="status", nullable = false, length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'DRAFT'")
	private String status = "DRAFT";

	@Column(name="credit_cost", nullable = false, columnDefinition = "INT DEFAULT 1")
	private Integer creditCost = 1;

	@Column(name="summary_title", length = 255)
	private String summaryTitle;

	@Column(name="summary_petition_type", length = 100)
	private String summaryPetitionType;

	@Column(name="summary_institution_name", length = 255)
	private String summaryInstitutionName;

	@Column(name="created_date")
	private LocalDateTime createdDate;

	@Column(name="updated_date")
	private LocalDateTime updatedDate;

	@Column(name="active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean active = true;

	//bi-directional one-to-one association to PetitionAgreements
	@OneToOne(mappedBy="petition", cascade = CascadeType.ALL)
	private PetitionAgreements petitionAgreements;

	//bi-directional one-to-one association to PetitionTypeInfo
	@OneToOne(mappedBy="petition", cascade = CascadeType.ALL)
	private PetitionTypeInfo petitionTypeInfo;

	//bi-directional one-to-one association to PetitionContent
	@OneToOne(mappedBy="petition", cascade = CascadeType.ALL)
	private PetitionContent petitionContent;

	//bi-directional one-to-many association to PetitionRequest
	@OneToMany(mappedBy="petition", cascade = CascadeType.ALL)
	private List<PetitionRequest> petitionRequests;

	//bi-directional one-to-many association to PetitionAttachment
	@OneToMany(mappedBy="petition", cascade = CascadeType.ALL)
	private List<PetitionAttachment> petitionAttachments;

	//bi-directional one-to-one association to PetitionPreferences
	@OneToOne(mappedBy="petition", cascade = CascadeType.ALL)
	private PetitionPreferences petitionPreferences;

	//bi-directional one-to-one association to PetitionIdentity
	@OneToOne(mappedBy="petition", cascade = CascadeType.ALL)
	private PetitionIdentity petitionIdentity;

	//bi-directional one-to-many association to CreditTransaction
	@OneToMany(mappedBy="petition")
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
