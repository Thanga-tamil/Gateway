package com.mirrorfly.gateway.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mirrorfly.gateway.dto.QueuePayloadDto;

public class DataHelperUtils {

	// Private constructor to prevent instantiation
	private DataHelperUtils() {
		throw new UnsupportedOperationException("Utility class - instantiation not allowed");
	}

	public static QueuePayloadDto prepareIQPayload(String userdomain, String username, String deviceId,
			Set<String> sessionIds) throws JsonProcessingException {
		Map<String, Object> iqPayloadMap = new HashMap<>();
		iqPayloadMap.put("username", username);
		iqPayloadMap.put("socket_id", String.join(",", sessionIds));
		iqPayloadMap.put("device_id", deviceId);
		return new QueuePayloadDto("logout_sessions", GatewayUtil.convertMapToJson(iqPayloadMap), userdomain);
	}

	public static QueuePayloadDto prepareIQPayload(String userdomain, String username, Set<String> sessionIds)
			throws JsonProcessingException {
		Map<String, Object> iqPayloadMap = new HashMap<>();
		iqPayloadMap.put("username", username);
		iqPayloadMap.put("socket_id", String.join(",", sessionIds));
		return new QueuePayloadDto("logout_sessions", GatewayUtil.convertMapToJson(iqPayloadMap), userdomain);
	}

}
