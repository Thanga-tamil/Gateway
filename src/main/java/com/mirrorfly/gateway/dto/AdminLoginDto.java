package com.mirrorfly.gateway.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AdminLoginDto {

	@NotNull(message = "Username cannot be Null")
	@NotEmpty(message = "Username cannot be Empty")
	private String username;

	@NotNull(message = "Password cannot be Null")
	@NotEmpty(message = "Password cannot be Empty")
	private String password;

}
