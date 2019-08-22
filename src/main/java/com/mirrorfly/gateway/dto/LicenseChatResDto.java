package com.mirrorfly.gateway.dto;
import lombok.Data;

@Data
public class LicenseChatResDto extends LicenseResDto{
	
	private String xmppDomain;
	
	private Integer xmppPort;
	
	private String baseUrl;

}
