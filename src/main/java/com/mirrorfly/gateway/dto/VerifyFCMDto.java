package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class VerifyFCMDto {

	private String mobileNumber;

	private String googleToken;

}
