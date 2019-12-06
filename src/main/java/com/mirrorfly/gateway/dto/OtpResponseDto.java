package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class OtpResponseDto {

	private String otp;

    private String apnsDeviceToken = "";
	
	private String voipDeviceToken = "";

	private Integer isUpdated = 0;

}
