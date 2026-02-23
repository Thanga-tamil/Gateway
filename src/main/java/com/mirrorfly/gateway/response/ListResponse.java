
package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(value = Include.NON_EMPTY)
@Data
public class ListResponse<T> {

	private Integer status;
	private T data;
	private String message;
	private Boolean error;
	private Integer count;
	private Integer totalRecords;

	public ListResponse() {
	}

	public ListResponse(T data) {
		this.data = data;
	}

	public ListResponse(Integer status, String message, Boolean error, Integer count, Integer totalRecords) {
		this.status = status;
		this.message = message;
		this.error = error;
		this.count = count;
		this.totalRecords = totalRecords;
	}

	public ListResponse(Integer status, T data, String message, Boolean error, Integer count, Integer totalRecords) {
		this.status = status;
		this.data = data;
		this.message = message;
		this.error = error;
		this.count = count;
		this.totalRecords = totalRecords;
	}

}
