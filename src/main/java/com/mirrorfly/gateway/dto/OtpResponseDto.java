package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class OtpResponseDto {

	private String otp;

	private String deviceToken;

}
