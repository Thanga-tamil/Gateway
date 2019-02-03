package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.repo.TokenRepo;

@Service
public class TokenValidation {

	@Autowired
	private TokenRepo tokenRepo;

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

		/*
		 * if (LocalDateTime.now().isAfter(token.getTokenExpiryTime())) {
		 * tokenRepo.delete(token); return null; } LocalDateTime tokenExtendTime =
		 * token.getTokenExpiryTime().minusMinutes(15); if
		 * (tokenExtendTime.isBefore(LocalDateTime.now())) {
		 * token.setTokenExpiryTime(LocalDateTime.now().plusMinutes(15));
		 * tokenRepo.save(token); }
		 */

		return token.getUsername();
	}

}
