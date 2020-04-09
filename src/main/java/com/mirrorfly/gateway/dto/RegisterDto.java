package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.Data;

@Data
public class RegisterDto {

	private String mobileNumber;

	private String otp;

	private String deviceType;

	private String mode;

	private String deviceToken;
	
	private String deviceOsVersion;

}
