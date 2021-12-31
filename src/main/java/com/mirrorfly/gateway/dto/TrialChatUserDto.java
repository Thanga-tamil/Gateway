package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class TrialChatUserDto {

 	private Integer totalActiveUserCount =  0;

	private Long totalUserCount;
	
	private Long activeUserCount;
	
	private String environment;

	private String licenseKey;
	
	private Integer activeWebUserCount =  0;

	private Integer activeIosUserCount = 0;
	
	private Integer activeAndroidUserCount = 0;
	
	private Integer newWebUserCount =  0;

	private Integer newIosUserCount = 0;
	
	private Integer newAndroidUserCount = 0;
	
}

