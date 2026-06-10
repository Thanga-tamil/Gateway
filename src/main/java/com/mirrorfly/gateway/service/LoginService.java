package com.mirrorfly.gateway.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import com.mirrorfly.gateway.dto.QueuePayloadDto;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

	private final AdminUserRepo adminUserRepo;

	private final VcardSearchRepo vcardSearchRepo;

	private final ChatUsersDetailsRepo chatUsersDetailsRepo;

	private final SpoolRepo spoolRepo;

	private final TokenService tokenService;

	private final QueueService queueService;

	private final EmailTokenService emailTokenService;

	private final EmailTokenRepo emailTokenRepo;
	
	private final Executor loginExecutor;
	 
	private final ApplicationProperties applicationProperties;

	private final DataStore dataStore;

	private final StringRedisTemplate stringRedisTemplate;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private static final String WEB = "web";

	@SwitchDataSource(value = "[0]")
	public Response<LoginDetailsDto> login(String userdomain, LoginDto loginRequestDto) {
	
		log.info("Login Request DTO: " + loginRequestDto);

		// Set DB based on domain
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		// Get license key
		String licenseKey = Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "licenseKey")).orElseThrow(() -> new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL));

		// Decrypt password if needed
		String password = decryptPassword(loginRequestDto.getPassword(), licenseKey);
		
		ChatUserDetails chatUserDetails = chatUsersDetailsRepo.findFirstByUsernameOrderByLastLoggedInAtDesc(loginRequestDto.getUsername()).orElseThrow(() -> new BadDataException(GatewayConstants.USER_NOT_FOUND));
		
		String user_password = WEB.equalsIgnoreCase(chatUserDetails.getDeviceType()) ? password : GatewayUtil.getMd5String(password);
		
		// Validate user
		if (!user_password.equals(chatUserDetails.getPassword()))
			throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
		
		// Check if user is blocked
		if (!loginRequestDto.getUsername().equals("123456789")) {
			VcardSearch vcardSearch = vcardSearchRepo.findByUsername(chatUserDetails.getUsername());
			if (1 == vcardSearch.getIsBlock()) {
				throw new ForbiddenException(GatewayConstants.BLOCK_USER);
			}
		}

		// Update last login
		chatUserDetails.setLastLoggedInAt(LocalDateTime.now());
		chatUsersDetailsRepo.save(chatUserDetails);

		String deviceId = StringUtils.isNotEmpty(chatUserDetails.getDeviceId()) ? chatUserDetails.getDeviceId() : "MF-01";
		String token = tokenService.asyncGetToken(userdomain, chatUserDetails.getUsername(), deviceId, "");

		LoginDetailsDto data = new LoginDetailsDto();
		data.setToken(token);

		Response<LoginDetailsDto> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setData(data);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	private String decryptPassword(String encryptedPassword, String licenseKey) {
		try {
			String decrypted = AESEncryption.decryptString(encryptedPassword, licenseKey.repeat(3));
			LoginRequestDto login = MAPPER.readValue(URLDecoder.decode(decrypted, StandardCharsets.UTF_8), LoginRequestDto.class);
			return login.getPassword();
		} catch (Exception e) {
			return encryptedPassword;
		}
	}

	public CompletableFuture<Response<LoginDetailsDto>> loginAsync(String userdomain, LoginDto loginRequestDto) {

		return CompletableFuture.supplyAsync(() -> {
			try {
				return processLogin(userdomain, loginRequestDto);
			} catch (BadDataException e) {
				return buildErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
			} catch (ForbiddenException e) {
				return buildErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
			} catch (Exception e) {
				return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
			}
		}, loginExecutor);
	}

	private Response<LoginDetailsDto> buildErrorResponse(int status, String message) {
		Response<LoginDetailsDto> resp = new Response<>();
		resp.setStatus(status);
		resp.setMessage(message);
		return resp;
	}
	 
    private Response<LoginDetailsDto> processLogin(String userdomain, LoginDto loginRequestDto) {

        // ------- Your login logic here ----------
        AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		String licenseKey = "DBYBps8EKEBUUGVEqK3bjruTk64iDx"; //Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "licenseKey")).orElseThrow(() -> new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL));

        String password = decryptPassword(loginRequestDto.getPassword(), licenseKey);

        String userPassword = GatewayUtil.getMd5String(password);

		ChatUserDetails chatUserDetails = chatUsersDetailsRepo.findByUsernameAndPassword(loginRequestDto.getUsername(), userPassword);

		if (chatUserDetails == null) {
			throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
		}

        if (!loginRequestDto.getUsername().equals("123456789")) {
            VcardSearch vcardSearch = vcardSearchRepo.findByUsername(chatUserDetails.getUsername());
            if (1 == vcardSearch.getIsBlock()) {
                throw new ForbiddenException(GatewayConstants.BLOCK_USER);
            }
        }

        chatUserDetails.setLastLoggedInAt(LocalDateTime.now());
        
        String deviceId = StringUtils.isNotEmpty(chatUserDetails.getDeviceId()) ? chatUserDetails.getDeviceId() : "MF-01";

        String token = "";
        log.info("is external authentication call? {}", loginRequestDto.getExtAuth());
        if (!loginRequestDto.getExtAuth()) {
            token = tokenService.asyncGetToken(userdomain, chatUserDetails.getUsername(), deviceId, "");
        }

        chatUsersDetailsRepo.save(chatUserDetails);
       
        LoginDetailsDto data = new LoginDetailsDto();
        data.setToken(token);
		
        Response<LoginDetailsDto> response = new Response<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage(GatewayConstants.DATA_RETRIEVED);
        response.setData(data);

        return response;
    }
    
	@SwitchDataSource(value = "[0]")
	public Response<String> logout(String userdomain, String authorization, Boolean simBindLogout) {
		
        AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		// Retrieve token details and remove token
		TokenDetail tokenDetail = tokenService.getTokenDetail(userdomain, authorization);
		tokenService.deleteToken(userdomain, authorization);

		if (tokenDetail == null) {
			return new Response<>(HttpStatus.OK.value(), GatewayConstants.LOG_OUT);
		}

		// Determine device ID
		String deviceId = StringUtils.defaultIfBlank(tokenDetail.getDevicetype(), "MF-01");

		// Fetch user session details
		ChatUserDetails chatUserDetail = chatUsersDetailsRepo.findByUsernameAndDeviceId(tokenDetail.getUsername(), deviceId);

		if (chatUserDetail != null) {
			String deviceType = chatUserDetail.getDeviceType();

			// Delete non-web device session
			// if (!GatewayConstants.DEVICE_WEB.equalsIgnoreCase(deviceType)) {
			chatUsersDetailsRepo.delete(chatUserDetail);
			// }

			// Delete offline spool data
			spoolRepo.deleteOfflilneRecordsByUsernameAndDeviceId(tokenDetail.getUsername(), deviceId);

			// Handle web session logout IQ event
			// if (GatewayConstants.DEVICE_WEB.equalsIgnoreCase(deviceType) && StringUtils.isNotBlank(chatUserDetail.getSocketId())) {
				try {
					Boolean delete = stringRedisTemplate.delete(authorization);
					log.info("logout: jwt deletion status in redis: {}", delete);
					QueuePayloadDto payload = DataHelperUtils.prepareIQPayload(userdomain, chatUserDetail.getUsername(), StringUtils.defaultIfEmpty(chatUserDetail.getDeviceId(), ""), Collections.singleton(StringUtils.defaultIfEmpty(chatUserDetail.getSocketId(), "")), simBindLogout);
					queueService.sendUserSessionLogoutActivity(payload);
				} catch (JsonProcessingException e) {
					log.error("Failed to send logout IQ payload: userdomain={}, username={}, socketId={}, error={}", userdomain, chatUserDetail.getUsername(), chatUserDetail.getSocketId(), e.getMessage());
				}
			// }
		}

		return new Response<>(HttpStatus.OK.value(), GatewayConstants.LOG_OUT);
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
		loginDetails.setToken(tokenService.asyncGetToken(userdomain, adminUser != null ? adminUser.getUsername() : "", "", ""));
		response.setStatus(HttpStatus.OK.value());
		response.setData(loginDetails);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public Response<String> sendForgotPasswordEmail(String emailId, String userdomain) {
		
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		AdminUser mail = adminUserRepo.findByEmailId(emailId);
		if (mail != null && mail.getIsActive() == 0) {
			throw new BadDataException(GatewayConstants.INACTIVE_ACCOUNT);
		}
		if (mail == null)
			throw new BadDataException(GatewayConstants.EMAILID_NOT_REGISTERED);

		String token = emailTokenService.generateToken(emailId, "forgot-password.html", userdomain);

		queueService.sendMailPayload(new MailPayload(GatewayConstants.FORGOT_PASWORD, emailId, mail.getName(), token, applicationProperties.getResetPasswordRedirectUrl()));

		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.EMAIL_SENT_SUCCESSFULLY);
		return response;
	}

	public Response<String> resetPassword(String token, PasswordDto passwordDto) {

		EmailToken validToken = emailTokenRepo.findByToken(token);

		if (validToken == null)
			throw new BadDataException("Please use the latest Reset Password link received in your email inbox or use the Forgot password option again to reset your password");

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
