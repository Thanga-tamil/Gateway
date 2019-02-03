package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_EMPTY)
public class Response<T> {
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     * @param status get the status value of processing
     * @param message get the message in string format
     * @param error get the error value of status
     */
	public Response(Integer status, String message, Boolean error) {
		super();
		this.status = status;
		this.message = message;
		this.error = error;
	}
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     * @param status get the status of processing
     * @param data get the data in json format
     * @param message get the message in string format
     * @param error get the error value of status
     */
	public Response(Integer status, T data, String message, Boolean error) {
		super();
		this.status = status;
		this.data = data;
		this.message = message;
		this.error = error;
	}
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     * @param data
     */
	public Response(T data) {
		super();
		this.data = data;
	}
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     */
	public Response() {
	}

	private Integer status;
	private T data;
	private String message;
	private Boolean error;
	/**
	 *  to return boolean value 
	 * @return error in boolean format
	 */
	public Boolean getError() {
		return error;
	}
	/**
	 * to set the parameter error boolean 
	 * @param error as true or false
	 */
	public void setError(Boolean error) {
		this.error = error;
	}
	/**
	 * to return status 
	 * @return return the status of processing
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * return the data 
	 * @return return the data json format
	 */
	public T getData() {
		return data;
	}
	/**
	 * return the message success/failed
	 * @return return found/or not found message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * set the processing status
	 * @param status is processing value
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * set the date to send in json format
	 * @param data in json format
	 */
	public void setData(T data) {
		this.data = data;
	}
	/**
	 * set the message to send
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
