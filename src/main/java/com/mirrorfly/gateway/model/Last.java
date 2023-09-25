package com.mirrorfly.gateway.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
	private String seconds;

	@Column(name = "presence")
	private Integer presence;

	@Column(name = "state")
	private String state;

}
