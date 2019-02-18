package com.mirrorfly.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VerifyFCMResponse {

	private String status;

	private String username;

	@JsonProperty(value = "device_token")
	private String deviceToken;

	private String message;

}
