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
@Table(name="basic_application")
@NamedQuery(name="BasicApplication.findAll", query="SELECT b FROM BasicApplication b")
public class BasicApplication implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(name="active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private boolean active = true;

	//bi-directional many-to-one association to Type
	@ManyToOne
	@JoinColumn(name="type_id")
	private Type type;

	@Column(name="type_other", length = 255)
	private String typeOther;

	//bi-directional many-to-one association to Firm
	@ManyToOne
	@JoinColumn(name="firm_id")
	private Firm firm;

	@Column(name="firm_other", length = 255)
	private String firmOther;

	@Column(name="title", nullable = false, length = 500)
	private String title;

	@Column(name="description", columnDefinition = "TEXT")
	private String description;

	@Column(name="created_by", length = 100)
	private String createdBy;

	@Column(name="created_date")
	private LocalDateTime createdDate;

	@Column(name="updated_by", length = 100)
	private String updatedBy;

	@Column(name="updated_date")
	private LocalDateTime updatedDate;

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