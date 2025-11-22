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
@Table(name="petition_type")
@NamedQuery(name="PetitionType.findAll", query="SELECT p FROM PetitionType p")
public class PetitionType implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@Column(name="name", length = 255)
	private String name;
}