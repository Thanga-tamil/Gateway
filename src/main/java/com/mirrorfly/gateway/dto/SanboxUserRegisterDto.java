package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class SanboxUserRegisterDto {

	private String mobileNumber;

	private String deviceType;
	
	private String deviceId = "";

	private int mode = 0;

	private String deviceToken = "";

	private String voipDeviceToken = "";

	private String deviceOsVersion = "";

	private String licenseKey = "";

	private String userIdentifier = "";

}
