package com.mirrorfly.gateway.service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;

@Service
public class PasswordEncryptionService {

	private PasswordEncryptionService() {
	}

	private static final int ITERATIONS = 65536;
	private static final int KEY_LENGTH = 256;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
	
	
	public static boolean verifyPassword(String password, String key, String salt) {
		Optional<String> optEncrypted = hashPassword(password, salt);
		if (!optEncrypted.isPresent())
			return false;
		return optEncrypted.get().equals(key);
	}

	/***
	 * Check the password pattern minimum one Caps, one Symbol, one number
	 * 
	 * @param password
	 * @return true message if password pattern matches
	 */

	public static boolean checkPasswordPattern(String password) {
		char ch;
		boolean capitalFlag = false;
		boolean symbolFlag = false;

		if (password.length() < 8)
			return false;

		Pattern p = Pattern.compile("[^A-Za-z]");
		Matcher m = p.matcher(password);
		symbolFlag = m.find();

		for (int i = 0; i < password.length(); i++) {
			ch = password.charAt(i);
			if (Character.isUpperCase(ch)) {
				capitalFlag = true;
			}
			if (capitalFlag && symbolFlag)
				return true;
		}
		return false;
	}

	/***
	 * Generate the password salt to Hash the password in future
	 * 
	 * @param length
	 * @return success message if password salt generated
	 */

	public static Optional<String> generateSalt(final int length) {

		final SecureRandom random = new SecureRandom();
		byte[] salt = new byte[length];
		random.nextBytes(salt);

		return Optional.of(Base64.getEncoder().encodeToString(salt));
	}

	

	/***
	 * Hashing the password while saves in DB for Security Purpose
	 * 
	 * @param password
	 * @param salt
	 * @return success message if password hashed
	 */

	public static Optional<String> hashPassword(String password, String salt) {
 		char[] chars = password.toCharArray();
		byte[] bytes = salt.getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

		Arrays.fill(chars, Character.MIN_VALUE);

		try {
			SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
			byte[] securePassword = fac.generateSecret(spec).getEncoded();
			return Optional.of(Base64.getEncoder().encodeToString(securePassword));

		} catch (Exception e) {
			return Optional.empty();

		} finally {
			spec.clearPassword();
		}
	}

}
