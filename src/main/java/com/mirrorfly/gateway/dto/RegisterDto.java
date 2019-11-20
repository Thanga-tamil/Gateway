package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.Data;

@Data
public class RegisterDto {

	@NotNull(message = GatewayConstants.MOBILE_REQUIRED)
	@NotBlank(message = GatewayConstants.MOBILE_NOT_VALID)
	private String mobileNumber;

	@NotBlank(message = GatewayConstants.OTP_NOT_VALID)
	private String otp;

	private String deviceType;

	private Integer mode;

	private String callDeviceToken;
	
	private String messageDeviceToken;

}
