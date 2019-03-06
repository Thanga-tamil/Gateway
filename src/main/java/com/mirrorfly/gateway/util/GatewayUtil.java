package com.mirrorfly.gateway.util;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;

public class GatewayUtil {

	private GatewayUtil() {

	}

	public static PageRequest getPagination(Integer page, Integer size) {
		page = page != null ? page : 0;
		size = size != null ? size : 10;
		return PageRequest.of(page, size);
	}

	public static LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now();
	}

	public static String getName(String firstName, String lastName) {
		return firstName + " " + lastName;
	}

}
