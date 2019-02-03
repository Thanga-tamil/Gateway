/*
 * @copyright Copyright (C) 2018 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.mirrorfly.gateway.exception;

/**
 * that is class user define exception to handle run time exception
 */
public class BadDataException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4958559663381805726L;

	public BadDataException(String message) {
		super(message);
	}
}
