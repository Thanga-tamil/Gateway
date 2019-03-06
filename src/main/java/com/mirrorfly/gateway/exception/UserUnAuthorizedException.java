package com.mirrorfly.gateway.exception;

public class UserUnAuthorizedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4108970394919771532L;

	public UserUnAuthorizedException(String message) {
		super(message);
	}

}
