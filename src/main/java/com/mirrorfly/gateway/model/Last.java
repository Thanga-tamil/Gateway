package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "last")
@Data
public class Last implements Serializable {

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
