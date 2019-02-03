
package com.mirrorfly.gateway.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

/**
 * A container class to decode user password for security reasons.
 *
 */

@Slf4j
public class PasswordDecode {

	/**
	 * Function to get Password hash using an algorithm.
	 * 
	 * @param text              input
	 * @param digestText        hash input
	 * @param algorithmInstance algorithm
	 * 
	 * @return string
	 */
	public static String getHash(String text, String digestText, String algorithmInstance) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithmInstance);
			md.update(text.getBytes());
			byte[] hasByte = md.digest(digestText.getBytes());
			return bytesToHexString(hasByte);
		} catch (NoSuchAlgorithmException objException) {
			log.error(objException.getMessage());
		}
		return null;
	}

	/**
	 * Function to get Password Hash using an Algorithm.
	 * 
	 * @param text              input
	 * @param algorithmInstance algorithm
	 * 
	 * @return string
	 */
	public static String getHash(String text, String algorithmInstance) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithmInstance);
			md.update(text.getBytes());
			byte[] hasByte = md.digest();
			return bytesToHexString(hasByte);
		} catch (NoSuchAlgorithmException objException) {
			log.error(objException.getMessage());
		}
		return null;
	}

	/**
	 * Function to get Byte information of String.
	 * 
	 * @param bytes byte
	 * @return string
	 */
	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}

	private PasswordDecode() {
	}
}
