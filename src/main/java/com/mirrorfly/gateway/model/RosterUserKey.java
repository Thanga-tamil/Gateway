package com.mirrorfly.gateway.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RosterUserKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7449975415288537692L;

	@Column(name = "username")
	private String username;

	@Column(name = "jid")
	private String jid;

}
