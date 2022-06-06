package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class RegisterDto {

	private String mobileNumber = "";

	private String deviceType;

	private Integer mode = 0;

	private String deviceToken = "";
	
	private String voipDeviceToken = "";
	
	private String deviceOsVersion = "";
	
	private String licenseKey = "";
	
 	private String userIdentifier = "";
	
 	private long currentTimestamp;
}
