package com.mirrorfly.gateway.dto;

import java.util.Map;

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
	
	private String deviceId = "";
	
	private Boolean forceRegister = true;
	
	private Map<String, Object> metaData;
	
}
