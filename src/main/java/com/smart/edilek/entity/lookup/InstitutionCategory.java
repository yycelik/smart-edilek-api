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
@Table(name="institution_category")
@NamedQuery(name="InstitutionCategory.findAll", query="SELECT i FROM InstitutionCategory i")
public class InstitutionCategory implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@Column(name="name", length = 255)
	private String name;
}