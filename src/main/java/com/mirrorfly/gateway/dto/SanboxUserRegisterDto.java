package com.mirrorfly.gateway.dto;

import java.util.Map;

import lombok.Data;

@Data
public class SanboxUserRegisterDto {

	private String mobileNumber;

	private String countryCode = "";

	private String countryCode = "";

	private String deviceType;
	
	private String deviceId = "";

	private int mode = 0;

	private String deviceToken = "";

	private String voipDeviceToken = "";

	private String deviceOsVersion = "";

	private String licenseKey = "";

	private String userIdentifier = "";
	
	private Boolean forceRegister = true;
	
	private Map<String, Object> metaData;
	
	private String pushServerType = "";
	
	private String userType;

}
