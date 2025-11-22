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
@Table(name="petition_preferences_date_format")
@NamedQuery(name="PetitionPreferencesDateFormat.findAll", query="SELECT p FROM PetitionPreferencesDateFormat p")
public class PetitionPreferencesDateFormat implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@Column(name="sample", length = 50)
	private String sample;
}