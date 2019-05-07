package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class VerifyFCMDto {

	@NotNull(message = "Enter your mobile number with country code")
	private String mobileNumber;

	@NotNull(message = "Enter your googleToken")
	private String googleToken;

}
