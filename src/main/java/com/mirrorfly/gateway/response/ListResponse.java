
package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
/**
 * This is a generic class, that acts as a container for any list of data, that needs to be sent in the response.
 * This is a class used for the send the response in json format
 * @param <T> the parameter of the class
 */
@JsonInclude(value = Include.NON_EMPTY)
public class ListResponse<T> {
	/**
     * Constructor of List Response which can invoke the method directly to activity
     *@param status get the status value
     *@param message get the message in string format
     * @param error get the error status 
     * @param count get the number of results
     * @param totalRecords get the total number of results
     */
	public ListResponse(Integer status, String message,Boolean error,Integer count,Integer totalRecords) {
		super();
		this.status = status;
		this.message = message;
		this.error=error;
		this.count=count;
		this.totalRecords=totalRecords;
	}
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     *@param data get the json data 
     *@param message get the message in string format
     *@param error get the error status
     * @param status get the status of processing
     * @param count get the number of result
     * @param totalRecords get the total number of results
     */
	public ListResponse(Integer status, T data, String message,Boolean error,Integer count,Integer totalRecords) {
		super();
		this.status = status;
		this.data = data;
		this.message = message;
		this.error=error;
		this.count=count;
		this.totalRecords=totalRecords;
	}
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     *@param data
     */
	public ListResponse(T data) {
		super();
		this.data = data;
	}
	/**
     * Constructor of List Response  which can invoke the method directly to activity
     */
	public ListResponse() {
	}

	private Integer status;
	private T data;
	private String message;
	private Boolean error;
	private Integer count;
	private Integer totalRecords;

	/**
	 * to return number of records
	 * @return count records
	 */
	public Integer getCount() {
		return count;
	}
	/**
	 * to return total records
	 * @return count total records
	 */
	public Integer getTotalRecords() {
		return totalRecords;
	}
	/**
	 * Method to use set the count 
	 * @param count
	 */
	public void setCount(Integer count) {
		this.count = count;
	}
	/**
	 * set the total records
	 * @param totalRecords
	 */
	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	/**
	 * return the boolean value
	 * @return true/false response
	 */
	public Boolean getError() {
		return error;
	}
	/**
	 * set the boolean value
	 * @param error
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
