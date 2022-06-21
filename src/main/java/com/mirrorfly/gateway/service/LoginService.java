package com.mirrorfly.gateway.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.AdminLoginDto;
import com.mirrorfly.gateway.dto.AdminLoginResponse;
import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.dto.LoginRequestDto;
import com.mirrorfly.gateway.dto.PasswordDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.AdminUser;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.EmailToken;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.queue.MailPayload;
import com.mirrorfly.gateway.queue.QueueService;
import com.mirrorfly.gateway.repo.AdminUserRepo;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.EmailTokenRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.repo.VcardSearch;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
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
	private UserRepo userRepo;

	@Autowired
	private ChatUserRepo chatUserRepo;

	@Autowired
	private EmailTokenService emailTokenService;

	@Autowired
	private EmailTokenRepo emailTokenRepo;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	public Response<LoginDetailsDto> login(LoginDto loginRequestDto) {
		// If incoming requested in encrypted means, we need to decrypt the password field to find the original password
		String password = "";
		String decyptedString = AESEncryption.decryptString(loginRequestDto.getPassword(),
				applicationProperties.getLicenseKey() + applicationProperties.getLicenseKey()+ applicationProperties.getLicenseKey());		 
		LoginRequestDto login;
		ObjectMapper mapper = new ObjectMapper();
		try {
			login = mapper.readValue(URLDecoder.decode(decyptedString, StandardCharsets.UTF_8.toString()),LoginRequestDto.class);
			password = login.getPassword();
			validateLoginRequestParams(login.getCurrentTimestamp());
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			log.error("Unable to unserialize the password field " + e.getMessage());
			password = loginRequestDto.getPassword();
		} 

		// Validating the user is blocked by admin or not
		Users users = userRepo.findByUsername(loginRequestDto.getUsername());
		if (users == null) {
			throw new BadDataException(GatewayConstants.USER_NAME_NOT_AVAIL);
		} else {
			VcardSearch vcardSearch = vcardSearchRepo.findByUsername(users.getUsername());
			if (vcardSearch != null && vcardSearch.getIsBlock() == 1) {
				throw new ForbiddenException(GatewayConstants.BLOCK_USER);
			}
		}

		// Validating the password against the data in DB
		Boolean isPasswordValid = false;
		if (users.getPassword() != null) {
			isPasswordValid = validatePasswordAgainstMd5(users.getPassword(), password);
		}
		if (Boolean.FALSE.equals(isPasswordValid) && users.getWsPassword() != null) {
			isPasswordValid = validatePasswordAgainstMd5(users.getWsPassword(), password);
		}
		if (Boolean.FALSE.equals(isPasswordValid)) {
			throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
		}

		Response<LoginDetailsDto> response = new Response<>();
		LoginDetailsDto loginDetails = new LoginDetailsDto();
		loginDetails.setToken(tokenService.getToken(loginRequestDto.getUsername(), "", ""));
		response.setStatus(HttpStatus.OK.value());
		response.setData(loginDetails);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}
 
	private Boolean validatePasswordAgainstMd5(String password, String userpassword) {
		// In DB we are having the Md5 string, so we are converting the password to Md5 String 
		// and validating it against the DB 	 
		return password.equals(GatewayUtil.getMd5String(userpassword));
	}

	private void validateLoginRequestParams(long latestTime) {
		Instant instant = Instant.now();
		if (latestTime < instant.minus(2, ChronoUnit.MINUTES).getEpochSecond() * 1000) {
			throw new BadDataException(GatewayConstants.REQUEST_TIMEOUT);
		}
	}	
	
	public Response<String> logout(String authorization) {
		String username = tokenService.getUserNameByToken(authorization);
		tokenService.deleteToken(authorization);
		if (username != null) {
			Optional<ChatUser> chatUser = chatUserRepo.findById(username);
			if (chatUser.isPresent()) {
				chatUser.get().setDeviceToken("");
				chatUser.get().setDevicetype("");
				chatUserRepo.save(chatUser.get());
				userRepo.updatePasswordByUsername(passwordService.generateRandomPassword(), username);
			}
		}
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.LOG_OUT);
		return response;
	}

	@SuppressWarnings("unused")
	public Response<AdminLoginResponse> adminLogin(AdminLoginDto login) {
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
		loginDetails.setToken(tokenService.getToken(adminUser != null ? adminUser.getUsername() : "", "", ""));
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
		adminUser.setHashedPassword(hashPassword);
		adminUser.setSaltPassword(saltValue.isPresent() ? saltValue.get() : "");
		adminUser.setUpdatedAt(LocalDateTime.now());
		adminUserRepo.save(adminUser);

		validToken.setStatus(GatewayConstants.STATUS_EXPIRED);
		validToken.setConfirmedDateTime(LocalDateTime.now());
		emailTokenRepo.save(validToken);
	}

}
