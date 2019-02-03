package com.mirrorfly.gateway.exception;

/**
 * This Class used to handle NoDataFound Exception at the runtime.
 *
 */
public class NoDataFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4108970394919771532L;

	public NoDataFoundException(String message) {
		super(message);
	}

}
