package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.smart.edilek.entity.lookup.PrivacyLevel;

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
@Table(name="petition_attachment")
@NamedQuery(name="PetitionAttachment.findAll", query="SELECT p FROM PetitionAttachment p")
public class PetitionAttachment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@Column(name="file_name", nullable = false, length = 255)
	private String fileName;

	@Column(name="file_path", nullable = false, length = 1000)
	private String filePath;

	@Column(name="file_type", length = 50)
	private String fileType;

	@Column(name="file_size_long")
	private Long fileSizeLong;

	@Column(name="title", length = 255)
	private String title;

	@Column(name="description", columnDefinition = "TEXT")
	private String description;

	@Column(name="upload_date")
	private LocalDateTime uploadDate;

	@ManyToOne
	@JoinColumn(name="privacy_level_id")
	private PrivacyLevel privacyLevel;

	@Column(name="evidence_types", length = 500)
	private String evidenceTypes;

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
		if (uploadDate == null) {
			uploadDate = LocalDateTime.now();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedDate = LocalDateTime.now();
	}
}