package com.mirrorfly.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "vcard")
@Data
public class Vcard implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "vcard")
	private String vcardName;

}
