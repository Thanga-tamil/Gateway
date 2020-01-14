package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AdminLoginDto {
	
	@NotNull(message = "Username cannot be Empty")
	private String username;

	@NotNull(message = "Password cannot be Empty")
	private String password;

}
