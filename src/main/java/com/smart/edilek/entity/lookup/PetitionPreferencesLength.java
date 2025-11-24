package com.smart.edilek.entity.lookup;

import java.io.Serializable;

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
@Table(name="petition_preferences_length")
@NamedQuery(name="PetitionPreferencesLength.findAll", query="SELECT p FROM PetitionPreferencesLength p")
public class PetitionPreferencesLength implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(name="code", length = 25)
	private String code;

	@Column(name="name", length = 100)
	private String name;

	@Column(name="description", length = 255)
	private String description;

	@Column(name="active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private Boolean active = true;
}