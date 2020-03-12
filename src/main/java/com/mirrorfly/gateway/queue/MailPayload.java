package com.mirrorfly.gateway.queue;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailPayload {
	
	private String emailType;

	private String emailId;

	private String name;

	private String token;

	private String redirectUrl;

}
