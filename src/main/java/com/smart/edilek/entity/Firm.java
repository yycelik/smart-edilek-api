package com.smart.edilek.entity;

import java.io.Serializable;
import java.util.List;

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
@Table(name="firm")
@NamedQuery(name="Firm.findAll", query="SELECT f FROM Firm f")
public class Firm implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@Column(name="active", columnDefinition = "TINYINT(1) DEFAULT 1")
	private boolean active = true;

	@Column(name="name", nullable = false, length = 255)
	private String name;

	//bi-directional many-to-one association to BasicApplication
	@OneToMany(mappedBy="firm")
	private List<BasicApplication> basicApplications;
}