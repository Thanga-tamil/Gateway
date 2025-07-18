package com.mirrorfly.gateway.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mirrorfly.gateway.dto.QueuePayloadDto;

public class DataHelperUtils {

	// Private constructor to prevent instantiation
	private DataHelperUtils() {
		throw new UnsupportedOperationException("Utility class - instantiation not allowed");
	}

	public static QueuePayloadDto prepareIQPayload(String userdomain, String username, Set<String> sessionIds)
			throws JsonProcessingException {
		Map<String, Object> iqPayloadMap = new HashMap<>();
		iqPayloadMap.put("username", username);
		String socketId = sessionIds == null ? "" : sessionIds.stream().filter(Objects::nonNull).collect(Collectors.joining(","));
		iqPayloadMap.put("socket_id", socketId);
		return new QueuePayloadDto("logout_sessions", GatewayUtil.convertMapToJson(iqPayloadMap), userdomain);
	}

}
