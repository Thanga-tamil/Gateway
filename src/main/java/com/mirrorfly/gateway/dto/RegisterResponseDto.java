package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class RegisterResponseDto {

	private String token;

	private String username;

	private String password;
	
	private Boolean isProfileUpdated = false;

}
