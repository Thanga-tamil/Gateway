package com.mirrorfly.gateway.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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

	@NotNull(message = "Username cannot be Null")
	@NotEmpty(message = "Username cannot be Empty")
	private String username;

	@NotNull(message = "Password cannot be Null")
	@NotEmpty(message = "Password cannot be Empty")
	private String password;

	private String type;

	private Boolean extAuth = false;

}