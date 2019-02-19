package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotNull;

import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.Data;

@Data
public class OtpDto {

	@NotNull(message = GatewayConstants.MOBILE_NO_AVAIL)
	private String mobileNumber;

	private String deviceType;

	private String otp;

	private String deviceToken;

}
