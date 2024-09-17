package com.mirrorfly.gateway.token;

import java.util.Date;
import java.util.Random;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.util.GatewayUtil;

/**
 * A Container class to generate token for authorization purpose. And with a
 * time limit to expire token.
 */

public class GenerateToken {

	private static final Random RANDOM = new Random();

	private GenerateToken() {
	}

	/**
	 * Function to generate Token for user logged in.
	 * 
	 * @param tokenDetail for login time new token will be generated.
	 * @return generated token
	 */
	public static String generateToken(TokenDetail tokenDetail) {
		Algorithm algorithm = Algorithm.HMAC256("mirror_gateway");
		long nowMillis = System.currentTimeMillis();
		Date expiresAt = new Date(nowMillis);
		return JWT.create().withIssuer("auth0").withExpiresAt(expiresAt)
				.withClaim("tempData", GatewayUtil.getAlphaNumericString(20))
				.withClaim("user", tokenDetail.getUsername()).sign(algorithm); // String -> token
	}

	public static String generateFileToken(String filename) {
		Algorithm algorithm = Algorithm.HMAC256("mirror_gateway");
		Date expiresAt = new Date();
		return JWT.create().withIssuer("auth0").withExpiresAt(expiresAt).withClaim("filename", filename)
				.sign(algorithm); // String -> file token
	}

	public static String generateOtpToken() {
		return String.valueOf(RANDOM.nextInt(999999 - 100000) + 100000); // 6-digit OTP
	}

}
