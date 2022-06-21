package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class SandboxRegisterResponseDto {

	private String token;

	private String username;

	private String password;

	private Boolean isExisting;

	private Boolean isProfileUpdated = false;

	private ConfigDto confiq;

}
