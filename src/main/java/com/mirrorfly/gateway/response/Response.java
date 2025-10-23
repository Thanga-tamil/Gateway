package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(value = Include.NON_EMPTY)
@Data
public class Response<T> {
	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param status  get the status value of processing
	 * @param message get the message in string format
	 * @param error   get the error value of status
	 */
	public Response(Integer status, String message, Boolean error) {
		super();
		this.status = status;
		this.message = message;
		this.error = error;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param status  get the status of processing
	 * @param data    get the data in json format
	 * @param message get the message in string format
	 * @param error   get the error value of status
	 */
	public Response(Integer status, T data, String message, Boolean error) {
		super();
		this.status = status;
		this.data = data;
		this.message = message;
		this.error = error;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param status  get the status of processing
	 * @param data    get the data in json format
	 * @param message get the message in string format
	 */
	public Response(Integer status, T data, String message) {
		super();
		this.status = status;
		this.data = data;
		this.message = message;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param data
	 */
	public Response(T data) {
		super();
		this.data = data;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 */
	public Response() {
	}

	private Integer status;
	private T data;
	private String message;
	private Boolean error;

}
