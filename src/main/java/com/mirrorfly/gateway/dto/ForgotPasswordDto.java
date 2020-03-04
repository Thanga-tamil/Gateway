package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class ForgotPasswordDto {
	
	private String name;
	
	private String emailId;
	
	private String token;
	
	private String resetPasswordRedirectUrl;

}
