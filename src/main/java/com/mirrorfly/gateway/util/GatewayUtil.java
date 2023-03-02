package com.mirrorfly.gateway.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.config.ApplicationProperties;

import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GatewayUtil {
	

	@Autowired
	private ApplicationProperties applicationProperties;

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
	
	public static String getMd5String(String input)
	{
		StringBuilder hashtext = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			hashtext.append(no.toString(16));
			while (hashtext.length() < 32) {
				hashtext.append("0" + hashtext);
			}
 		}
 		catch (NoSuchAlgorithmException e) {
 			log.error("Error while create string to MD5");
			log.error(e.getMessage());
		}
		return hashtext.toString();
	}
	
	public Boolean userIpAccessValidation(String remoteIP) {
		IPAddressString ipAddress = new IPAddressString(remoteIP);
		String[] array = applicationProperties.getWhiteListIps().split(",");
		for (String rangeIp : array) {
			IPAddressString range = new IPAddressString(rangeIp);
			if (range.contains(ipAddress)) {
				return true;
			}
		}
		return false;
	}

}
