package com.mirrorfly.gateway.service;

import java.time.Duration;
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
import com.mirrorfly.gateway.model.ChatEmailTokens;
import com.mirrorfly.gateway.repo.ChatEmailTokensRepository;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailAuthService {

	@Autowired
	private AWSEmailService awsEmailService;

	@Autowired
	private ChatEmailTokensRepository chatEmailTokensRepository;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	@Autowired
	private DataStore dataStore;

	@Autowired
	private ConfigService configService;

	@Autowired
	private CommonService commonService;

	@Transactional
	@SwitchDataSource(value = "[0]")
	public Response<Object> sendOtpViaMail(String userdomain, String email) {
		validateEmail(email);

		ChatEmailTokens emailToken = saveOrUpdateOtpInDb(email);
		// Send Mail with the generated 6-digit OTP
		try {
			awsEmailService.sendOtpEmail(email, emailToken.getOtp());
			return buildResponse(HttpStatus.OK, GatewayConstants.OTP_SENT_SUCCESS,
					"OTP sent successfully to: " + email);
		} catch (MessagingException e) {
			log.error("Error occurred while sending OTP email: " + e.getMessage());
			// Roll-back transaction if sending email fails
			throw new MessagingException("Failed to send OTP email ==> " + email);
		}
	}

	private ChatEmailTokens saveOrUpdateOtpInDb(String email) {

		long expirationTime = System.currentTimeMillis() + Duration.ofMinutes(10).toMillis(); // OTP Expiration Time

		return chatEmailTokensRepository.findByEmail(email).map(existingToken -> {
			// Reuse existing OTP if not expired and expiration time has has than 5 minutes
			if (!existingToken.isOtpExpired() && existingToken.hasMoreThanFiveMinutes()
					&& existingToken.getIsVerified() != 1 && existingToken.getVerifyAttempt() < 3) {
				log.info("Reusing existing OTP for email: {}", email);
				return existingToken;
			}
			// Generate new OTP and update expiration
			return updateExistingToken(existingToken, expirationTime);

		}).orElseGet(() -> createNewToken(email, expirationTime));
	}

	private ChatEmailTokens updateExistingToken(ChatEmailTokens token, long expirationTime) {
		token.setOtp(GenerateToken.generateOtpToken());
		token.setVerifyAttempt(0);
		token.setIsVerified(0);
		token.setExpiresAt(expirationTime); // Set expiration time directly
		log.info("Updated OTP and expiration for email: {}", token.getEmail());
		return chatEmailTokensRepository.save(token);
	}

	private ChatEmailTokens createNewToken(String email, long expirationTime) {
		ChatEmailTokens newOtpToken = new ChatEmailTokens();
		newOtpToken.setEmail(email);
		newOtpToken.setOtp(GenerateToken.generateOtpToken());
		newOtpToken.setExpiresAt(expirationTime); // Set expiration time directly
		log.info("Created new OTP and expiration for email: {}", email);
		return chatEmailTokensRepository.save(newOtpToken);
	}

	@SwitchDataSource(value = "[0]")
	public Response<Object> validateEmailOtp(String userdomain, String email, String otp) {

		log.info("Starting OTP validation for email: {}", email);
		// Validate the inputs
		validateInputs(email, otp);
		ChatEmailTokens emailToken = findOtpToken(email);
		/*
		 * Validate the given is valid and not expired
		 */
		validateOtp(emailToken, otp);

		String licenseKey = (String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain,
				GatewayConstants.LICENSEKEY);

		ChatSettings chatSettings = configService.getChatSettingsData(licenseKey);
		/*
		 * Enable Encryption mode aes-256-cbc in the MySQL Server
		 */
		commonService.enableEncryptionMode();

		String username = findOrGenerateUsername(email, chatSettings.getIvProfile());
		log.info("Username determined for email {}: {}", email, username);

		updateOtpTokenVerificationStatus(emailToken);
		log.info("OTP token verification status updated for email: {}", email);

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

	private ChatEmailTokens findOtpToken(String email) {
		log.info("Fetching OTP token for email: {}", email);
		return chatEmailTokensRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Invalid OTP for email: {}", email);
			return new BadDataException(GatewayConstants.EMAIL_INVALID);
		});
	}

	private void validateOtp(ChatEmailTokens emailToken, String otp) {
		if (emailToken.getVerifyAttempt() >= 3) {
			log.error("Verify OTP limit exceeded for email: {}", emailToken.getEmail());
			throw new BadDataException(GatewayConstants.VERIFY_OTP_LIMIT_REACHED);
		}
		if (!emailToken.getOtp().equals(otp)) {
			updateVerifyAttempts(emailToken); // Update the Invalid OTP attempt count
			chatEmailTokensRepository.save(emailToken);
			throw new BadDataException(GatewayConstants.INVALID_OTP);
		}
		if (emailToken.getIsVerified() == 1) {
			log.error("OTP already verified for email: {}", emailToken.getEmail());
			throw new BadDataException(GatewayConstants.INVALID_OTP);
		}
		if (emailToken.isOtpExpired()) {
			log.error("OTP expired for email: {}", emailToken.getEmail());
			throw new BadDataException(GatewayConstants.OTP_EXPIRED);
		}
		log.debug("OTP validated successfully for email: {}", emailToken.getEmail());
	}

	// Update verify attempts and handle limit exceed
	private void updateVerifyAttempts(ChatEmailTokens emailToken) {
		Integer attempt = emailToken.getVerifyAttempt() == null ? 0 : emailToken.getVerifyAttempt() + 1;
		emailToken.setVerifyAttempt(attempt);
		log.warn("Invalid OTP attempt {} for email: {}", attempt, emailToken.getEmail());
		chatEmailTokensRepository.save(emailToken); // Save the updated attempts count
	}

	// Update OTP token verification status
	private void updateOtpTokenVerificationStatus(ChatEmailTokens emailToken) {
		emailToken.setIsVerified(1); // Set the token as verified
		chatEmailTokensRepository.save(emailToken);
	}

	/*
	 * Find exiting userId from the v_card_search if not found generate new userId
	 */
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
