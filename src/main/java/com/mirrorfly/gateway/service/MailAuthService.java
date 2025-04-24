package com.mirrorfly.gateway.service;

import java.time.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.EmailConfig;
import com.mirrorfly.gateway.model.ChatEmailTokens;
import com.mirrorfly.gateway.repo.ChatEmailTokensRepository;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
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

		log.info("Starting OTP generation for email: {}", email);
		validateEmail(email);

		// Fetch connection configuration from the data store
		ConnectionConfig config = getConnectionConfigByDomain(userdomain);

		// Determine email configuration
		EmailConfig emailConfig = config.getEmailConfig() != null ? config.getEmailConfig()
				: fetchEmailConfig(userdomain);

		if (emailConfig == null) {
			log.warn("No email configuration found for domain: {}", userdomain);
			return buildResponse(HttpStatus.BAD_REQUEST, "No email configuration found for domain: " + userdomain,
					null);
		}

		if (isStaticEmail(emailConfig, email)) {
			log.info("Static email detected, skipping OTP generation for email: {}", email);
			return buildResponse(HttpStatus.OK, GatewayConstants.OTP_SENT_SUCCESS,
					"OTP sent successfully to: " + email);
		}

		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		String otpToken = saveOrUpdateOtpInDb(email, config.getLicenseKey());
		// Send Mail with the generated 6-digit OTP
		try {
			awsEmailService.sendAWSSES(emailConfig, email, otpToken);
			log.info("OTP sent successfully to email: {}", email);
			return buildResponse(HttpStatus.OK, GatewayConstants.OTP_SENT_SUCCESS,
					"OTP sent successfully to: " + email);
		} catch (MessagingException e) {
			log.error("Error occurred while sending OTP email: " + e.getMessage());
			// Roll-back transaction if sending email fails
			throw new MessagingException("Failed to send OTP email ==> " + e.getMessage());
		}
	}

	private String saveOrUpdateOtpInDb(String email, String licenseKey) {

		long expirationTime = System.currentTimeMillis() + Duration.ofMinutes(10).toMillis(); // OTP Expiration Time

		return chatEmailTokensRepository.findByEmail(AESEncryption.encryptString(email, licenseKey))
				.map(existingToken -> {
					// Reuse existing OTP if not expired and expiration time has has than 5 minutes
					if (!existingToken.isOtpExpired() && existingToken.hasMoreThanFiveMinutes()
							&& existingToken.getIsVerified() != 1 && existingToken.getVerifyAttempt() < 3) {
						log.info("Reusing existing OTP for email: {}", email);
						return AESEncryption.decryptString(existingToken.getOtp(), licenseKey);
					}
					// Generate new OTP and update expiration
					return updateExistingToken(existingToken, licenseKey, expirationTime);

				}).orElseGet(() -> createNewToken(email, licenseKey, expirationTime));
	}

	private String updateExistingToken(ChatEmailTokens token, String licenseKey, long expirationTime) {
		String otoToken = GenerateToken.generateOtpToken();
		token.setOtp(AESEncryption.encryptString(otoToken, licenseKey));
		token.setVerifyAttempt(0);
		token.setIsVerified(0);
		token.setExpiresAt(expirationTime); // Set expiration time directly
		log.info("Updated OTP and expiration for email: {}", token.getEmail());
		chatEmailTokensRepository.save(token);
		return otoToken;
	}

	private String createNewToken(String email, String licenseKey, long expirationTime) {
		ChatEmailTokens emailOtptoken = new ChatEmailTokens();
		String otpToken = GenerateToken.generateOtpToken();
		emailOtptoken.setEmail(AESEncryption.encryptString(email, licenseKey));
		emailOtptoken.setOtp(AESEncryption.encryptString(otpToken, licenseKey));
		emailOtptoken.setExpiresAt(expirationTime); // Set expiration time directly
		log.info("Created new OTP and expiration for email: {}", email);
		chatEmailTokensRepository.save(emailOtptoken);
		return otpToken;
	}

	@SwitchDataSource(value = "[0]")
	public Response<Object> validateEmailOtp(String userdomain, String email, String otp) {

		log.info("Starting OTP validation for email: {}", email);
		// Validate the inputs
		validateInputs(email, otp);

		ConnectionConfig config = getConnectionConfigByDomain(userdomain);

		EmailConfig emailConfig = Optional.ofNullable(config.getEmailConfig()).orElseGet(() -> {
			log.warn("EmailConfig is null for domain '{}'. Current ConnectionConfig: {}", userdomain, config);
			return fetchEmailConfig(userdomain);
		});

		if (emailConfig == null) {
			log.warn("No email configuration found for domain: {}", userdomain);
			return buildResponse(HttpStatus.BAD_REQUEST, "No email configuration found for domain: " + userdomain,
					null);
		}

		if (isStaticEmail(emailConfig, email) && otp.equals(emailConfig.getStaticMetadata().getStaticOtp())) {
			log.info("Static email and OTP detected, skipping validation for email: {}", email);
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("username", emailConfig.getStaticMetadata().getStaticUserId());
			responseData.put("email", email);
			return buildResponse(HttpStatus.OK, GatewayConstants.OTP_VERIFIED_SUCCESS, responseData);
		}

		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		ChatEmailTokens emailToken = findOtpToken(AESEncryption.encryptString(email, config.getLicenseKey()));
		/*
		 * validate the OTP
		 */
		validateOtp(emailToken, otp, config.getLicenseKey());

		ChatSettings chatSettings = configService.getChatSettingsData(config.getLicenseKey());
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
		responseData.put("email", email);
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

	private ConnectionConfig getConnectionConfigByDomain(String userdomain) {
		return Optional.ofNullable(dataStore.getConnectionConfigByDomain(userdomain)).orElseThrow(() -> {
			log.warn("No connection configuration found for domain: {}", userdomain);
			return new IllegalArgumentException("No connection configuration found for domain: " + userdomain);
		});
	}

	private EmailConfig fetchEmailConfig(String userdomain) {

		EntityManagerFactory entityManagerFactory = null;
		EntityManager entityManager = null;

		try {
			// Set the database to default
			entityManagerFactory = dataStore.createMasterEntityManagerFactory();
			entityManager = entityManagerFactory.createEntityManager();

			return entityManagerFactory.createEntityManager()
					.createQuery("SELECT c.emailConfig FROM ConnectionConfig c WHERE c.apiDomain = :apiDomain",
							EmailConfig.class)
					.setParameter("apiDomain", userdomain).getSingleResult();
		} catch (NoResultException e) {
			log.warn("Email configuration found for domain: {}", userdomain);
			return null;
		} catch (Exception e) {
			log.error("Failed to fetch email configuration for domain: {}", userdomain, e);
			return null;

		} finally {
			if (entityManager != null && entityManager.isOpen())
				entityManager.close();

			if (entityManagerFactory != null)
				entityManagerFactory.close();
		}
	}

	private boolean isStaticEmail(EmailConfig emailConfig, String email) {
		return emailConfig != null && emailConfig.getStaticMetadata() != null
				&& email.equalsIgnoreCase(emailConfig.getStaticMetadata().getStaticEmail());
	}

	private ChatEmailTokens findOtpToken(String email) {
		log.info("Fetching OTP token for email: {}", email);
		return chatEmailTokensRepository.findByEmail(email).orElseThrow(() -> {
			log.error("Invalid OTP for email: {}", email);
			return new BadDataException(GatewayConstants.EMAIL_INVALID);
		});
	}

	private void validateOtp(ChatEmailTokens emailToken, String otp, String licenseKey) {
		String email = AESEncryption.decryptString(emailToken.getEmail(), licenseKey);
		if (emailToken.getVerifyAttempt() >= 3) {
			log.error("Verify OTP limit exceeded for email: {}", email);
			throw new BadDataException(GatewayConstants.VERIFY_OTP_LIMIT_REACHED);
		}
		if (!emailToken.getOtp().equals(AESEncryption.encryptString(otp, licenseKey))) {
			updateVerifyAttempts(emailToken); // Update the Invalid OTP attempt count
			chatEmailTokensRepository.save(emailToken);
			log.warn("Invalid OTP attempt {} for email: {}", emailToken.getVerifyAttempt(), email);
			throw new BadDataException(GatewayConstants.INVALID_OTP);
		}
		if (emailToken.getIsVerified() == 1) {
			log.error("OTP already verified for email: {}", email);
			throw new BadDataException(GatewayConstants.INVALID_OTP);
		}
		if (emailToken.isOtpExpired()) {
			log.error("OTP expired for email: {}", email);
			throw new BadDataException(GatewayConstants.OTP_EXPIRED);
		}
		log.debug("OTP validated successfully for email: {}", email);
	}

	// Update verify attempts and handle limit exceed
	private void updateVerifyAttempts(ChatEmailTokens emailToken) {
		Integer attempt = emailToken.getVerifyAttempt() == null ? 0 : emailToken.getVerifyAttempt() + 1;
		emailToken.setVerifyAttempt(attempt);
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
		String username = vcardSearchRepo.findUsernameByEmail(email, ivProfile);
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
