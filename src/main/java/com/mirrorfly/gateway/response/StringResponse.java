package com.mirrorfly.gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(value = Include.NON_EMPTY)
@Data
public class StringResponse {

	private Integer status;
	private String message;
	private Boolean error;

	public StringResponse() {
	}

	public StringResponse(Integer status, String message, Boolean error) {
		this.status = status;
		this.message = message;
		this.error = error;
	}

}
