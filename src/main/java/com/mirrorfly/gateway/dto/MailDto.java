package com.mirrorfly.gateway.dto;

import java.util.Map;

import lombok.Data;

@Data
public class MailDto {

	private String from;

	private String toAddress;

	private String subject;

	private String firstName;

	private String lastName;
	
	private String password;

	private String template;

	private Map<String, Object> model;

}
