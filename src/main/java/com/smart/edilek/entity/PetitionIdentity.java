package com.smart.edilek.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smart.edilek.entity.lookup.ApplicantType;
import com.smart.edilek.entity.lookup.SignatureType;

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
@Table(name="petition_identity")
@NamedQuery(name="PetitionIdentity.findAll", query="SELECT p FROM PetitionIdentity p")
public class PetitionIdentity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@OneToOne
	@JoinColumn(name="petition_id", nullable = false)
	private Petition petition;

	@ManyToOne
	@JoinColumn(name="applicant_type_id")
	private ApplicantType applicantType;

	// Individual fields
	@Column(name="first_name", length = 255)
	private String firstName;

	@Column(name="last_name", length = 255)
	private String lastName;

	@Column(name="national_id", length = 20)
	private String nationalId;

	@Column(name="birth_date")
	private LocalDate birthDate;

	@Column(name="phone", length = 50)
	private String phone;

	@Column(name="email", length = 255)
	private String email;

	// Corporate fields
	@Column(name="company_name", length = 255)
	private String companyName;

	@Column(name="tax_number", length = 50)
	private String taxNumber;

	@Column(name="authorized_person", length = 255)
	private String authorizedPerson;

	// Address fields
	@Column(name="city", length = 100)
	private String city;

	@Column(name="district", length = 100)
	private String district;

	@Column(name="postal_code", length = 20)
	private String postalCode;

	@Column(name="address", columnDefinition = "TEXT")
	private String address;

	@ManyToOne
	@JoinColumn(name="signature_type_id")
	private SignatureType signatureType;

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