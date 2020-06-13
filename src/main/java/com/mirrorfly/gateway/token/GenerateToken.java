package com.mirrorfly.gateway.token;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mirrorfly.gateway.model.TokenDetail;

/**
 * A Container class to generate token for authorization purpose. And with a
 * time limit to expire token.
 */

public class GenerateToken {

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
		String token = JWT.create().withIssuer("auth0").withExpiresAt(expiresAt)
				.withClaim("user", tokenDetail.getUsername()).sign(algorithm);
		return token;

	}

	public static String generateFileToken(String filename) {
		Algorithm algorithm = Algorithm.HMAC256("mirror_gateway");
		Date expiresAt = new Date();
		String token = JWT.create().withIssuer("auth0").withExpiresAt(expiresAt).withClaim("filename", filename)
				.sign(algorithm);
		return token;
	}

}
