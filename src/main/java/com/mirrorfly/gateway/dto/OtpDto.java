package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class OtpDto {

	private String mobileNumber;

	private String deviceType;

	private String otp;

	private String deviceToken;

}
