package com.mirrorfly.gateway.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class EmailConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private String accessKeyId;
	private String secretAccessKey;
	private String region;
	private String defaultFromAddress;
	private String subject;
	private String emailTemplate;
	private StaticMetadata staticMetadata;

	@Data
	public static class StaticMetadata implements Serializable {

		private static final long serialVersionUID = 1L;

		private String staticEmail;
		private String staticOtp;
		private String staticUserId;
	}
}
