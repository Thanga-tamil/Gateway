package com.mirrorfly.gateway.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDto {

	private String mobileNumber = "";

	private String countryCode = "";

	private String countryCode = "";

	private String deviceType;

	private Integer mode = 0;

	private String deviceToken = "";

	private String voipDeviceToken = "";

	private String deviceOsVersion = "";

	private String licenseKey = "";

	private String userIdentifier = "";
	
	private String deviceId = "";
	
    private Boolean forceRegister = true;
    
    private Map<String, Object> metaData = null;
    
	private String pushServerType = "";
    
    private String userType = "";
}
