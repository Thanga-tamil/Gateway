package com.mirrorfly.gateway.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;

public class GatewayUtil {

	private GatewayUtil() {
	}

	private static SecureRandom rnd = new SecureRandom();

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

	// function to generate a random string of length n
	public static String getAlphaNumericString(int n) {
		String alphaNumericString = "0123456789" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + System.currentTimeMillis()
				+ "abcdefghijklmnopqrstuvxyz" + "0123456789";
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			sb.append(alphaNumericString.charAt(rnd.nextInt(alphaNumericString.length())));
		}
		return sb.toString();
	}

}
