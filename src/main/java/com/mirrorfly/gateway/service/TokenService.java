package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.repo.TokenRepo;
import com.mirrorfly.gateway.token.GenerateToken;
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

}
