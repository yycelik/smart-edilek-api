package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.smart.edilek.entity.lookup.PetitionType;
import com.smart.edilek.entity.lookup.InstitutionCategory;

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
@Table(name="petition_type_info")
@NamedQuery(name="PetitionTypeInfo.findAll", query="SELECT p FROM PetitionTypeInfo p")
public class PetitionTypeInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@OneToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@ManyToOne
	@JoinColumn(name="petition_type_id", nullable = false)
	private PetitionType petitionType;

	@Column(name="custom_type_name", length = 255)
	private String customTypeName;

	@ManyToOne
	@JoinColumn(name="institution_category_id")
	private InstitutionCategory institutionCategory;

	@Column(name="institution_name", length = 255)
	private String institutionName;

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
