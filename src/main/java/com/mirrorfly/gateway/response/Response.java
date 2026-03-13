package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(value = Include.NON_EMPTY)
@Data
public class Response<T> {

	private Integer status;
	private T data;
	private String message;
	private Boolean error;

	public Response() {
	}

	public Response(T data) {
		this.data = data;
	}

	public Response(Integer status, String message) {
		this.status = status;
		this.message = message;
	}

	public Response(Integer status, String message, Boolean error) {
		this.status = status;
		this.message = message;
		this.error = error;
	}

	public Response(Integer status, T data, String message, Boolean error) {
		this.status = status;
		this.data = data;
		this.message = message;
		this.error = error;
	}

	public Response(Integer status, T data, String message) {
		this.status = status;
		this.data = data;
		this.message = message;
	}

}
