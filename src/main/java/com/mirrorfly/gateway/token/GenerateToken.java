
package com.mirrorfly.gateway.token;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
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
		JWTSigner signer = new JWTSigner("mirror_gateway");
		Map<String, Object> claims = new HashMap<>();
		claims.put("user", tokenDetail.getUsername());
		JWTSigner.Options option = new JWTSigner.Options();
		option.setAlgorithm(Algorithm.HS256);
		option.setIssuedAt(true);
		option.setJwtId(true);
		option.setNotValidBeforeLeeway(3);
		return signer.sign(claims, option);
	}

}
