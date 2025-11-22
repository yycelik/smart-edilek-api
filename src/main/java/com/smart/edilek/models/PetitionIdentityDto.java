package com.smart.edilek.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smart.edilek.models.lookup.ApplicantTypeDto;
import com.smart.edilek.models.lookup.SignatureTypeDto;

import lombok.Getter;
import lombok.Setter;

public class PetitionIdentityDto {

	@Getter @Setter
	private String id;

	@Getter @Setter
	private String petitionId;

	@Getter @Setter
	private ApplicantTypeDto applicantType;

	// Individual fields
	@Getter @Setter
	private String firstName;

	@Getter @Setter
	private String lastName;

	@Getter @Setter
	private String nationalId;

	@Getter @Setter
	private LocalDate birthDate;

	@Getter @Setter
	private String phone;

	@Getter @Setter
	private String email;

	// Corporate fields
	@Getter @Setter
	private String companyName;

	@Getter @Setter
	private String taxNumber;

	@Getter @Setter
	private String authorizedPerson;

	// Address fields
	@Getter @Setter
	private String city;

	@Getter @Setter
	private String district;

	@Getter @Setter
	private String postalCode;

	@Getter @Setter
	private String address;

	@Getter @Setter
	private SignatureTypeDto signatureType;

	@Getter @Setter
	private LocalDateTime createdDate;

	@Getter @Setter
	private LocalDateTime updatedDate;

	@Getter @Setter
	private boolean active;
}