package com.mirrorfly.gateway.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mirrorfly.gateway.util.GatewayConstants;

import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

/**
 * A Container class to generate token for authorization purpose. And with a
 * time limit to expire token.
 */

public class GenerateToken {

	private static final SecureRandom RANDOM = new SecureRandom();

	private GenerateToken() {
	}

	/**
	 * Function to generate Token for user logged in.
	 * 
	 * @param tokenDetail for login time new token will be generated.
	 * @return generated token
	 */
	public static String generateToken(String jid, byte[] SecretKey) {
		Algorithm algorithm = Algorithm.HMAC256(SecretKey);

		long now = System.currentTimeMillis();
		Date expiresAt = new Date(now + (60L * 60 * 1000)); // 1 hour

		return JWT.create()
				.withClaim("jid", jid)
				.withExpiresAt(expiresAt)
				.sign(algorithm);
	}

	public static String generateOtpToken() {
		return String.valueOf(RANDOM.nextInt(999999 - 100000) + 100000); // 6-digit OTP
	}

	public static String generateRandomUsername(int length) {
		StringBuilder username = new StringBuilder();
		// Generate random alphanumeric characters
		for (int i = 0; i < length - 4; i++) { // Leave space for a UUID fragment for uniqueness
			int index = RANDOM.nextInt(GatewayConstants.ALPHA_NUMERIC.length());
			username.append(GatewayConstants.ALPHA_NUMERIC.charAt(index));
		}
		// Add a portion of a UUID to ensure uniqueness
		String uuidFragment = UUID.randomUUID().toString().substring(0, 4); // Using a 4-character fragment from UUID
		username.append(uuidFragment);
		return username.toString();
	}

}
