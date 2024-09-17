package com.mirrorfly.gateway.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.OtpTokens;
import com.mirrorfly.gateway.repo.OtpTokensRepository;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailAuthService {

	@Autowired
	private AWSEmailService awsEmailService;

	@Autowired
	private OtpTokensRepository otpTokensRepository;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DataStore dataStore;

	@Autowired
	private ConfigService configService;

	@Transactional
	@SwitchDataSource(value = "[0]")
	public Response<Object> sendOtpViaMail(String userdomain, String email) {
		validateEmail(email);
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

	@Transactional
	@SwitchDataSource(value = "[0]")
	public Response<Object> validateEmailOtp(String userdomain, String email, String otp) {

		log.info("Starting OTP validation for email: {}", email);
		// Validate the inputs
		validateInputs(email, otp);
		OtpTokens otpToken = findOtpToken(email);
		/*
		 * Validate the given is valid and not expired
		 */
		validateOtp(otpToken, otp);

		String licenseKey = (String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain,
				GatewayConstants.LICENSEKEY);

		ChatSettings chatSettings = configService.getChatSettingsData(licenseKey);

		enableEncryptionMode(); // Enable Encryption mode aes-256-cbc in the MySQL Server
		String username = findOrGenerateUsername(email, chatSettings.getIvProfile());
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("username", username);
		return buildResponse(HttpStatus.OK, GatewayConstants.OTP_VERIFIED_SUCCESS, responseData);
	}

	// Validate the Email
	private void validateEmail(String email) {
		if (StringUtils.isBlank(email) || !GatewayUtil.validateEmailBasedOnRegex(GatewayConstants.EMAIL_REGEX, email))
			throw new BadDataException(GatewayConstants.EMAIL_INVALID);
	}

	// Validate the Email and OTP
	private void validateInputs(String email, String otp) {
		validateEmail(email);
		if (StringUtils.isBlank(otp))
			throw new BadDataException(GatewayConstants.INVALID_OTP);
	}

	private OtpTokens findOtpToken(String email) {
		log.info("Fetching OTP token for email: {}", email);
		return otpTokensRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Invalid OTP for email: {}", email);
			return new BadDataException(GatewayConstants.EMAIL_INVALID);
		});
	}

	private void validateOtp(OtpTokens otpToken, String otp) {
		if (!otpToken.getOtp().equals(otp)) {
			log.error("Invalid OTP provided for email: {}", otpToken.getEmail());
			throw new BadDataException(GatewayConstants.INVALID_OTP);
		}
		if (otpToken.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
			log.error("OTP expired for email: {}", otpToken.getEmail());
			throw new BadDataException(GatewayConstants.OTP_EXPIRED);
		}
		log.debug("OTP validated successfully for email: {}", otpToken.getEmail());
	}

	private void enableEncryptionMode() {
		log.debug("Enabling aes-256-cbc encryption mode on MySQL server");
		entityManager.createNativeQuery("SET block_encryption_mode ='aes-256-cbc';").executeUpdate();
		log.info("Encryption mode set to aes-256-cbc");
	}

	private String findOrGenerateUsername(String email, String ivProfile) {
		log.debug("Searching for username in VCardSearch for email: {}", email);
		String username = vcardSearchRepo.getUsernamesByEmail(email, ivProfile);
		if (StringUtils.isBlank(username)) {
			log.info("Username not found. Generating a new username for email: {}", email);
			username = GenerateToken.generateRandomUsername(10); // Generate a 10-character random user-name
		} else
			log.debug("Username found: {}", username);
		return username;
	}

	private Response<Object> buildResponse(HttpStatus status, String message, Object data) {
		Response<Object> response = new Response<>();
		response.setStatus(status.value());
		response.setMessage(message);
		response.setData(data);
		return response;
	}

}
