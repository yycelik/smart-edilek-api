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
@Table(name="petition_ai_history")
@NamedQuery(name="PetitionAiHistory.findAll", query="SELECT p FROM PetitionAiHistory p")
public class PetitionAiHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@Column(name="ai_result", columnDefinition = "TEXT")
	private String aiResult;

	@Column(name="created_date")
	private LocalDateTime createdDate;

	@PrePersist
	protected void onCreate() {
		createdDate = LocalDateTime.now();
	}
}
