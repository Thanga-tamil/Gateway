package com.mirrorfly.gateway.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users_contacts")
@Data
public class UserContact extends AbstractEntity<String> implements Serializable {

	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "username")
	private String username;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "name")
	private String name;

	@Column(name = "license_key")
	private String licenseKey;

	public UserContact() {
	}

	public UserContact(String id, String username, String name, String mobileNumber, String licenseKey) {
		this.id = id;
		this.username = username;
		this.name = name;
		this.mobileNumber = mobileNumber;
		this.licenseKey = licenseKey;
	}

	@Override
	public String getId() {
		return id;
	}
}
