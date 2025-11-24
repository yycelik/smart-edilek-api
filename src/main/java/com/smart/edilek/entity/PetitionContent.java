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
@Table(name="petition_content")
@NamedQuery(name="PetitionContent.findAll", query="SELECT p FROM PetitionContent p")
public class PetitionContent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@OneToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@Column(name="title", nullable = false, length = 255)
	private String title;

	@Column(name="summary", nullable = false, columnDefinition = "TEXT")
	private String summary;

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
