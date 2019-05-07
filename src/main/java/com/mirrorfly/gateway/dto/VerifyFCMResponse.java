package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class VerifyFCMResponse {

	private String status;

	private String username;

	private String deviceToken;

	private String message;

}
