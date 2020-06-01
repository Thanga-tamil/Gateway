package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class RegisterDto {

	private String mobileNumber;

	private String otp;

	private String deviceType;

	private String mode;

	private String deviceToken = "";
	
	private String voipDeviceToken = "";
	
	private String deviceOsVersion = "";

}
