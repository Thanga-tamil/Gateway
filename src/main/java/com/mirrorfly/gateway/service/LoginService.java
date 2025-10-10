package com.mirrorfly.gateway.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.dto.AdminLoginDto;
import com.mirrorfly.gateway.dto.AdminLoginResponse;
import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.dto.LoginRequestDto;
import com.mirrorfly.gateway.dto.PasswordDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.AdminUser;
import com.mirrorfly.gateway.model.ChatUserDetails;
import com.mirrorfly.gateway.model.EmailToken;
import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.model.VcardSearch;
import com.mirrorfly.gateway.queue.MailPayload;
import com.mirrorfly.gateway.queue.QueueService;
import com.mirrorfly.gateway.repo.AdminUserRepo;
import com.mirrorfly.gateway.repo.ChatUsersDetailsRepo;
import com.mirrorfly.gateway.repo.EmailTokenRepo;
import com.mirrorfly.gateway.repo.SpoolRepo;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.DataHelperUtils;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginService {

	@Autowired
	private AdminUserRepo adminUserRepo;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private QueueService queueService;

	@Autowired
	private EmailTokenService emailTokenService;

	@Autowired
	private EmailTokenRepo emailTokenRepo;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	@Autowired
	private ChatUsersDetailsRepo chatUsersDetailsRepo;

	@Autowired
	private DataStore dataStore;

	@Autowired
	private SpoolRepo spoolRepo;

	@SwitchDataSource(value = "[0]")
	public Response<LoginDetailsDto> login(String userdomain, LoginDto loginRequestDto) {
		// If incoming requested in encrypted means, we need to decrypt the password
		// field to find the original password
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		Object config = dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "licenseKey");
		String licenseKey = config != null ? (String) config : null;
		if (licenseKey == null) {
			throw new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL);
		}

		String password = "";
		String decyptedString = AESEncryption.decryptString(loginRequestDto.getPassword(),
				licenseKey + licenseKey + licenseKey);
		LoginRequestDto login;
		ObjectMapper mapper = new ObjectMapper();
		try {
			login = mapper.readValue(URLDecoder.decode(decyptedString, StandardCharsets.UTF_8.toString()),
					LoginRequestDto.class);
			password = login.getPassword();
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			log.error("Unable to unserialize the password field " + e.getMessage()
					+ ", continuing assuming the password is plain");
			password = loginRequestDto.getPassword();
		}

		// Validating the password against the data in DB
		ChatUserDetails chatUserDetail = chatUsersDetailsRepo.findByUsernameAndPassword(loginRequestDto.getUsername(),
				GatewayUtil.getMd5String(password));
		if (chatUserDetail == null) {
			throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
		} else {
			// Validating the user is blocked by admin or not
			VcardSearch vcardSearch = vcardSearchRepo.findByUsername(chatUserDetail.getUsername());
			if (vcardSearch != null && vcardSearch.getIsBlock() == 1) {
				throw new ForbiddenException(GatewayConstants.BLOCK_USER);
			}
			chatUserDetail.setLastLoggedInAt(LocalDateTime.now());
			chatUsersDetailsRepo.save(chatUserDetail);
		}

		Response<LoginDetailsDto> response = new Response<>();
		LoginDetailsDto loginDetails = new LoginDetailsDto();
		String deviceId = chatUserDetail.getDeviceId() != null && !chatUserDetail.getDeviceId().isEmpty()
				? chatUserDetail.getDeviceId()
				: "MF-01";
		loginDetails.setToken(tokenService.getToken(userdomain, chatUserDetail.getUsername(), deviceId, ""));
		response.setStatus(HttpStatus.OK.value());
		response.setData(loginDetails);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	@SwitchDataSource(value = "[0]")
	public Response<String> logout(String userdomain, String authorization) {

		TokenDetail tokenDetail = tokenService.getTokenDetail(userdomain, authorization);
		tokenService.deleteToken(userdomain, authorization);

		if (tokenDetail != null) {
			String deviceId = tokenDetail.getDevicetype() != null && !tokenDetail.getDevicetype().isEmpty()
					? tokenDetail.getDevicetype()
					: "MF-01";
			ChatUserDetails chatUserDetail = chatUsersDetailsRepo.findByUsernameAndDeviceId(tokenDetail.getUsername(),
					deviceId);
			if (chatUserDetail != null) {

				chatUsersDetailsRepo.delete(chatUserDetail);
				spoolRepo.deleteOfflilneRecordsByUsernameAndDeviceId(tokenDetail.getUsername(), deviceId);

				// If QR-Web session (SocketId) send session logged-out IQ to the devices
//				if (chatUserDetail.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB)
//						&& StringUtils.isNotBlank(chatUserDetail.getSocketId())) {
					try {
						queueService.sendUserSessionLogoutActivity(DataHelperUtils.prepareIQPayload(userdomain,
								chatUserDetail.getUsername(), Collections.singleton(chatUserDetail.getSocketId())));
					} catch (JsonProcessingException e) {
						log.error("Failed to convert IQ payload to JSON: userdomain={}, username={}, sessionIds={}",
								userdomain, chatUserDetail.getUsername(), chatUserDetail.getSocketId(), e.getMessage());
					}
				}

//			}
		}

		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.LOG_OUT);
		return response;
	}

	@SwitchDataSource(value = "[0]")
	public Response<AdminLoginResponse> adminLogin(String userdomain, AdminLoginDto login) {
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		AdminUser adminUser = null;
		if (login.getUsername() != null) {
			adminUser = adminUserRepo.findByUsername(login.getUsername());
			if (adminUser == null)
				adminUser = adminUserRepo.findByEmailId(login.getUsername());

			if (adminUser != null && adminUser.getIsActive() == 0) {
				throw new BadDataException(GatewayConstants.INACTIVE_ACCOUNT);
			}
			if (null == adminUser) {
				throw new BadDataException(GatewayConstants.INVALID_CREDENTAILS);
			}

			boolean validPassword = PasswordEncryptionService.verifyPassword(login.getPassword(),
					adminUser.getHashedPassword(), adminUser.getSaltPassword());

			if (!validPassword)
				throw new BadDataException(GatewayConstants.WRONG_PASWORD);

		}
		Response<AdminLoginResponse> response = new Response<>();
		AdminLoginResponse loginDetails = new AdminLoginResponse();
		loginDetails.setImage(adminUser != null ? adminUser.getImage() : "");
		loginDetails
				.setToken(tokenService.getToken(userdomain, adminUser != null ? adminUser.getUsername() : "", "", ""));
		response.setStatus(HttpStatus.OK.value());
		response.setData(loginDetails);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public Response<String> sendForgotPasswordEmail(String emailId) {
		AdminUser mail = adminUserRepo.findByEmailId(emailId);
		if (mail != null && mail.getIsActive() == 0) {
			throw new BadDataException(GatewayConstants.INACTIVE_ACCOUNT);
		}
		if (mail == null)
			throw new BadDataException(GatewayConstants.EMAILID_NOT_REGISTERED);

		String token = emailTokenService.generateToken(emailId, "forgot-password.html");

		queueService.sendMailPayload(new MailPayload(GatewayConstants.FORGOT_PASWORD, emailId, mail.getName(), token,
				applicationProperties.getResetPasswordRedirectUrl()));

		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.EMAIL_SENT_SUCCESSFULLY);
		return response;
	}

	public Response<String> resetPassword(String token, PasswordDto passwordDto) {

		EmailToken validToken = emailTokenRepo.findByToken(token);

		if (validToken == null)
			throw new BadDataException("You can't be able to Reset Password");

		AdminUser adminUser = adminUserRepo.findByEmailId(validToken.getEmailId());
		if (adminUser != null && adminUser.getIsActive() == 0) {
			throw new BadDataException(GatewayConstants.INACTIVE_ACCOUNT);
		}
		validatePassword(token, passwordDto);

		/*** Password Hashing ***/

		/*** End ***/

		savePassword(passwordDto, validToken, adminUser);

		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.PASS_CONFIRMATION_SUCCESS);
		return response;
	}

	public Response<String> activateAccount(String token, PasswordDto passwordDto) {
		EmailToken validToken = emailTokenRepo.findByToken(token);

		if (validToken == null)
			throw new BadDataException("You can't be able to Activate Your Account");

		validatePassword(token, passwordDto);

		AdminUser adminUser = adminUserRepo.findByEmailId(validToken.getEmailId());

		savePassword(passwordDto, validToken, adminUser);

		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.ACCOUNT_ACTIVATION_SUCCESS);
		return response;
	}

	private void validatePassword(String token, PasswordDto passwordDto) {
		EmailToken validToken = emailTokenRepo.findByTokenAndStatus(token, "PENDING");

		if (validToken == null)
			throw new BadDataException("Link Expired");

		if (passwordDto.getNewPassword() == null || passwordDto.getNewPassword().isEmpty())
			throw new BadDataException("Please Enter the New Password");

		if (passwordDto.getConfirmPassword() == null || passwordDto.getConfirmPassword().isEmpty())
			throw new BadDataException("Please Enter the Confirm Password");

		if (!passwordDto.getNewPassword().equalsIgnoreCase(passwordDto.getConfirmPassword()))
			throw new BadDataException(GatewayConstants.PASS_CONFIRMATION);

		boolean validPasswordPattern = PasswordEncryptionService.checkPasswordPattern(passwordDto.getNewPassword());

		if (!validPasswordPattern)
			throw new BadDataException(GatewayConstants.PASWORD_PATTERN_MISMATCH);

	}

	private void savePassword(PasswordDto passwordDto, EmailToken validToken, AdminUser adminUser) {
		/*** Password Hashing ***/

		Optional<String> saltValue = PasswordEncryptionService.generateSalt(32);
		Optional<String> passwordValue = PasswordEncryptionService.hashPassword(passwordDto.getNewPassword(),
				saltValue.isPresent() ? saltValue.get() : "");

		String hashPassword = null;
		if (passwordValue.isPresent())
			hashPassword = passwordValue.get();

		/*** End ***/

		if (adminUser != null) {
			adminUser.setHashedPassword(hashPassword);
			adminUser.setSaltPassword(saltValue.isPresent() ? saltValue.get() : "");
			adminUser.setUpdatedAt(LocalDateTime.now());
		}
		adminUserRepo.save(adminUser);

		validToken.setStatus(GatewayConstants.STATUS_EXPIRED);
		validToken.setConfirmedDateTime(LocalDateTime.now());
		emailTokenRepo.save(validToken);
	}

	public Response<String> refreshInMemory() {
		dataStore.reloadConnectionConfig();
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.DOMAIN_CONFIG_RELOADED);
		return response;
	}

}
