package com.mirrorfly.gateway.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "user_roles")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserRoles {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer roleId;

	@Column(name = "name")
	private String roleName;

	@Column(name = "slug")
	private String slug;

	@Column(name = "permission")
	private String permission;

	@Column(name = "is_active")
	private String is_active;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	
}
