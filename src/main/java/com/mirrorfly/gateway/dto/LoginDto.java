package com.mirrorfly.gateway.dto;

import lombok.Data;

/**
 * A container to store and validate user detail like email and password for
 * login connections.
 * 
 * A DTO class used to login page to retrieve email and password informations.
 * 
 */

@Data
public class LoginDto {

	private String username = "";

	private String password = "";

	private String type;

}