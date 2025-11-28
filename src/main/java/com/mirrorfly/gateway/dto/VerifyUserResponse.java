package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class VerifyUserResponse {

	private String deviceToken;

	private Boolean validateOTP;

}
