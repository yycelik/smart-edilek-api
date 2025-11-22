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
@Table(name="applicant_type")
@NamedQuery(name="ApplicantType.findAll", query="SELECT a FROM ApplicantType a")
public class ApplicantType implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", length = 255)
	private String id;

	@Column(name="name", length = 100)
	private String name;
}