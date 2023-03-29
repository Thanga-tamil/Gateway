package com.mirrorfly.gateway.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.exception.UserUnAuthorizedException;
import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.repo.TokenRepo;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenService {

	@Autowired
	private TokenRepo tokenRepo;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * A Function to Validate token information with header and expire time limit.
	 * 
	 * 
	 * @param header as Bearer in JSON input type
	 */

	public String validateToken(String token) {
		log.info("requesting token: " + token);
		try {
			String u = stringRedisTemplate.opsForValue().get(token);
			return u != null ? u : getTokenFromDB(token);
		} catch (Exception e) {
			log.error("Error in redis connection {}", e.getMessage());
			return getTokenFromDB(token);
		}
	}

	public void delete(String token) {
		try {
			stringRedisTemplate.delete(token);
		} catch (Exception e) {
			log.error("Error in redis connection ", e);
		}
	}

	public String getToken(String username, String devicetype, String clientIp) {
		TokenDetail tokenDetail = new TokenDetail();
		tokenDetail.setClientIp(clientIp);
		tokenDetail.setCreatedAt(GatewayUtil.getCurrentDateTime());
		tokenDetail.setDevicetype(devicetype);
		tokenDetail.setTokenExpiryTime(GatewayUtil.getCurrentDateTime().plusHours(1l));
		tokenDetail.setToken(GenerateToken.generateToken(tokenDetail));
		tokenDetail.setUsername(username);
		tokenRepo.save(tokenDetail);
		try {
			stringRedisTemplate.opsForValue().set(tokenDetail.getToken(), username, 1, TimeUnit.HOURS);
		} catch (Exception e) {
			log.error("Error in redis connection ", e);
			return tokenDetail.getToken();
		}
		return tokenDetail.getToken();

	}

	private String getTokenFromDB(String header) {
		TokenDetail token = tokenRepo.findTop1ByTokenOrderByIdDesc(header);
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
	
	public TokenDetail getTokenDetail(String token) {
		return tokenRepo.findTop1ByTokenOrderByIdDesc(token);
	}

}
