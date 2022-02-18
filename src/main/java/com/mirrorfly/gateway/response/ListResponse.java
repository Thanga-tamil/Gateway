
package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * This is a generic class, that acts as a container for any list of data, that
 * needs to be sent in the response. This is a class used for the send the
 * response in json format
 * 
 * @param <T> the parameter of the class
 */
@JsonInclude(value = Include.NON_EMPTY)
@Data
public class ListResponse<T> {
	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param status       get the status value
	 * @param message      get the message in string format
	 * @param error        get the error status
	 * @param count        get the number of results
	 * @param totalRecords get the total number of results
	 */
	public ListResponse(Integer status, String message, Boolean error, Integer count, Integer totalRecords) {
		super();
		this.status = status;
		this.message = message;
		this.error = error;
		this.count = count;
		this.totalRecords = totalRecords;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param data         get the json data
	 * @param message      get the message in string format
	 * @param error        get the error status
	 * @param status       get the status of processing
	 * @param count        get the number of result
	 * @param totalRecords get the total number of results
	 */
	public ListResponse(Integer status, T data, String message, Boolean error, Integer count, Integer totalRecords) {
		super();
		this.status = status;
		this.data = data;
		this.message = message;
		this.error = error;
		this.count = count;
		this.totalRecords = totalRecords;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 * 
	 * @param data
	 */
	public ListResponse(T data) {
		super();
		this.data = data;
	}

	/**
	 * Constructor of List Response which can invoke the method directly to activity
	 */
	public ListResponse() {
	}

	private Integer status;
	private T data;
	private String message;
	private Boolean error;
	private Integer count;
	private Integer totalRecords;

}
