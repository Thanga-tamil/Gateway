package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "last")
@Data
public class Last implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "status")
	private Integer status;

	@Column(name = "seconds")
	private BigInteger seconds;

	@Column(name = "presence")
	private Integer presence;

	@Column(name = "state")
	private String state;

}
