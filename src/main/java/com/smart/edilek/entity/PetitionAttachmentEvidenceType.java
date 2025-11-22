package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.smart.edilek.entity.lookup.EvidenceType;

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
@Table(name="petition_attachment_evidence_type")
@NamedQuery(name="PetitionAttachmentEvidenceType.findAll", query="SELECT p FROM PetitionAttachmentEvidenceType p")
public class PetitionAttachmentEvidenceType implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@ManyToOne
	@JoinColumn(name="petition_attachment_id", nullable = false)
	private PetitionAttachment petitionAttachment;

	@ManyToOne
	@JoinColumn(name="evidence_type_id", nullable = false)
	private EvidenceType evidenceType;

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
