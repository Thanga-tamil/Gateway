package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class SettingsResponseDto {

	private String mailDomain;

	private String adminUser;

	private Integer audioLimit;

	private Integer videoLimit;

	private String mailLogin;

}
