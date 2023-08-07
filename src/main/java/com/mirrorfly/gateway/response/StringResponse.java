package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(value = Include.NON_EMPTY)
@Data
public class StringResponse {
	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param status  get the status value of processing
	 * @param message get the message in string format
	 * @param error   get the error value of status
	 */
	public StringResponse(Integer status, String message, Boolean error) {
		super();
		this.status = status;
		this.message = message;
		this.error = error;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 */
	public StringResponse() {
	}

	private Integer status;
	private String message;
	private Boolean error;

}
