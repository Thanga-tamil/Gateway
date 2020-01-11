package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AdminLoginDto {

	private String username;

	private String emailId;

	@NotNull(message = "Password cannot be Empty")
	private String password;

}
