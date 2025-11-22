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
@Table(name="petition_mode")
@NamedQuery(name="PetitionMode.findAll", query="SELECT p FROM PetitionMode p")
public class PetitionMode implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@Column(name="description", length = 255)
	private String description;
}