package com.mirrorfly.gateway.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.model.EmailToken;
import com.mirrorfly.gateway.repo.EmailTokenRepo;
import com.mirrorfly.gateway.util.GatewayConstants;

@Service
public class EmailTokenService {

	@Autowired
	private EmailTokenRepo emailTokenRepo;

	public String generateToken(String emailId, String templateName) {

		EmailToken emailToken = emailTokenRepo.findByEmailIdAndEmailTemplate(emailId, templateName);
		if (emailToken == null) {
			emailToken = new EmailToken();
			emailToken.setEmailId(emailId);
			emailToken.setEmailTemplate(templateName);
		}
		emailToken.setStatus(GatewayConstants.STATUS_PENDING);
		emailToken.setToken(UUID.randomUUID().toString());
		emailToken.setIssuedDateTime(LocalDateTime.now());
		emailToken.setExpiredDateTime(LocalDateTime.now().plusDays(1));
		emailToken = emailTokenRepo.save(emailToken);
		return emailToken.getToken();
	}
}