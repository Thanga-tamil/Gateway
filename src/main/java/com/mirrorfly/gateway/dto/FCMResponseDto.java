package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class FCMResponseDto {

	private String kind;

	/*
	 * "users": [ { "localId": string, "email": string, "emailVerified": boolean,
	 * "displayName": string, "providerUserInfo": [ { "providerId": string,
	 * "displayName": string, "photoUrl": string, "federatedId": string } ],
	 * "photoUrl": string, "passwordHash": bytes, "salt": bytes, "version": integer,
	 * "passwordUpdatedAt": double "validSince": string } ]
	 */
}
