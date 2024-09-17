package com.mirrorfly.gateway.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.OtpTokens;
import com.mirrorfly.gateway.repo.OtpTokensRepository;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailAuthService {

	@Autowired
	private OtpTokensRepository otpTokensRepository;

	@Autowired
	private AWSEmailService awsEmailService;

	@Transactional
	@SwitchDataSource(value = "[0]")
	public Response<Object> sendOtpViaMail(String userdomain, String email) {

		if (StringUtils.isBlank(email) || !GatewayUtil.validateEmailBasedOnRegex(GatewayConstants.EMAIL_REGEX, email))
			throw new BadDataException(GatewayConstants.EMAIL_INVALID);

		String generatedOtp = GenerateToken.generateOtpToken(); // Generate 6-digit OTP
		// Save the generated OTP in the DB for Verification
		saveOrUpdateOtpInDb(email, generatedOtp);
		// Send Mail with the generated 6-digit OTP
		try {
			awsEmailService.sendOtpEmail(email, generatedOtp);
			return buildResponse(HttpStatus.OK, GatewayConstants.OTP_SENT_SUCCESS,
					"OTP sent successfully to: " + email);
		} catch (MessagingException e) {
			log.error("Error occurred while sending OTP email: " + e.getMessage());
			// Roll-back transaction if sending email fails
			throw new MessagingException("Failed to send OTP email ==> " + email);
		}
	}

	private OtpTokens saveOrUpdateOtpInDb(String email, String otp) {

		return otpTokensRepository.findByEmail(email).map(existingToken -> {
			existingToken.setOtp(otp);
			log.info("Updated OTP in the database for email: {}", email);
			return otpTokensRepository.save(existingToken);
		}).orElseGet(() -> {
			OtpTokens newOtpToken = new OtpTokens();
			newOtpToken.setEmail(email);
			newOtpToken.setOtp(otp);
			log.info("Saved new OTP in the database for email: {}", email);
			return otpTokensRepository.save(newOtpToken);
		});

	}

	private Response<Object> buildResponse(HttpStatus status, String message, Object data) {
		Response<Object> response = new Response<>();
		response.setStatus(status.value());
		response.setMessage(message);
		response.setData(data);
		return response;
	}

}
