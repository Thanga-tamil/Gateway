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
	
	// function to generate a random string of length n
	public static String getAlphaNumericString(int n) {
            // chose a Character random from this String
            String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + System.currentTimeMillis()
                            + "abcdefghijklmnopqrstuvxyz";
            // create StringBuffer size of AlphaNumericString
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                    // generate a random number between
                    // 0 to AlphaNumericString variable length
                    int index = (int) (alphaNumericString.length() * Math.random());
                    // add Character one by one in end of sb
                    sb.append(alphaNumericString.charAt(index));
            }
            return sb.toString();
    }


}
