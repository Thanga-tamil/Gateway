package com.mirrorfly.gateway.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.exception.UserUnAuthorizedException;
import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.repo.TokenRepo;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

@Service
public class TokenService {

	@Autowired
	private TokenRepo tokenRepo;

	public String getToken(String username, String devicetype, String clientIp) {
		TokenDetail tokenDetail = new TokenDetail();
		tokenDetail.setClientIp(clientIp);
		tokenDetail.setCreatedAt(GatewayUtil.getCurrentDateTime());
		tokenDetail.setDevicetype(devicetype);
		tokenDetail.setTokenExpiryTime(GatewayUtil.getCurrentDateTime().plusHours(1l));
		tokenDetail.setToken(GenerateToken.generateToken(tokenDetail));
		tokenDetail.setUsername(username);
		tokenRepo.save(tokenDetail);
		return tokenDetail.getToken();

	}

	/**
	 * A Function to Validate token information with header and expire time limit.
	 * 
	 * 
	 * @param header as Bearer in JSON input type
	 */

	public String validateToken(String header) {
		TokenDetail token = tokenRepo.findByToken(header);
		if (token == null)
			return null;

		if (LocalDateTime.now().isAfter(token.getTokenExpiryTime())) {
			tokenRepo.delete(token);
			return null;
		}
		LocalDateTime tokenExtendTime = token.getTokenExpiryTime().minusMinutes(15);
		if (tokenExtendTime.isBefore(LocalDateTime.now())) {
			token.setTokenExpiryTime(LocalDateTime.now().plusMinutes(15));
			tokenRepo.save(token);
		}

		return token.getUsername();
	}

	public String getUserNameByToken(String authorization) {
		String username = validateToken(authorization);
		if (username == null) {
			throw new UserUnAuthorizedException(GatewayConstants.UNAUTHORIZED);
		}
		return username;
	}

	public void deleteToken(String authorization) {
		TokenDetail token = tokenRepo.findByToken(authorization);
		if (token != null)
			tokenRepo.delete(token);
	}
}
