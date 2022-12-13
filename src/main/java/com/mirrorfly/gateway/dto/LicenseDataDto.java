package com.mirrorfly.gateway.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class LicenseDataDto {

	private Timestamp expiresAt;

	private List<String> modulesPurchased;

	private List<String> domains;

	private Integer isActive;

	private String xmppDomain;

	private String xmppHost;

	private String signalUrl;

	private String janusUrl;

	private String port;

	private String portWeb;

	private String iv;

	private String ivProfile;
	
	private String apiUsername;
	
	private String apiPassword;
	
	private Integer isApiPasswordRevoked;
}
