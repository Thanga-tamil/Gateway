package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * A container to store and validate user detail like email and password for
 * login connections.
 * 
 * A DTO class used to login page to retrieve email and password informations.
 * 
 */

@Data
public class LoginDto {

	@NotNull(message = "UserName cannot be Empty")
	private String username;

	@NotNull(message = "Password cannot be Empty")
	private String password;

	private String type;

}