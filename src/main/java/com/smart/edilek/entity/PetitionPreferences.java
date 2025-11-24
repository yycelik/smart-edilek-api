package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.smart.edilek.entity.lookup.PetitionPreferencesStyle;
import com.smart.edilek.entity.lookup.PetitionPreferencesLanguage;
import com.smart.edilek.entity.lookup.PetitionPreferencesLength;
import com.smart.edilek.entity.lookup.PetitionPreferencesDateFormat;

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
@Table(name="petition_preferences")
@NamedQuery(name="PetitionPreferences.findAll", query="SELECT p FROM PetitionPreferences p")
public class PetitionPreferences implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@OneToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@ManyToOne
	@JoinColumn(name="style_id")
	private PetitionPreferencesStyle style;

	@ManyToOne
	@JoinColumn(name="language_id")
	private PetitionPreferencesLanguage language;

	@ManyToOne
	@JoinColumn(name="length_id")
	private PetitionPreferencesLength length;

	@Column(name="paragraphs", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean paragraphs = true;

	@Column(name="bullet_points", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean bulletPoints = false;

	@Column(name="legal_references", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean legalReferences = false;

	@ManyToOne
	@JoinColumn(name="date_format_id")
	private PetitionPreferencesDateFormat dateFormat;

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