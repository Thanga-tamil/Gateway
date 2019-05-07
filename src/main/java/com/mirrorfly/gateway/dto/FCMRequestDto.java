package com.mirrorfly.gateway.dto;

import java.util.List;

import lombok.Data;

@Data
public class FCMRequestDto {

	private String idToken;

	private String localId;

	private List<String> email;

}
