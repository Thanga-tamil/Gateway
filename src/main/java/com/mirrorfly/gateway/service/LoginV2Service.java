package com.mirrorfly.gateway.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.dto.LoginRequestDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.Users;
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
public class LoginV2Service {

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	public Response<LoginDetailsDto> login(LoginDto loginRequestDto) {
		// If incoming requested in encrypted means, we need to decrypt the password field to find the original password
		String decyptedString = AESEncryption.decryptString(loginRequestDto.getPassword(),
				applicationProperties.getLicenseKey() + applicationProperties.getLicenseKey()+ applicationProperties.getLicenseKey());
		if (decyptedString.equals("")) {
			log.error("Unable to decrypt the password field ");
			throw new BadDataException(GatewayConstants.INVALID_ERROR);
		}
		LoginRequestDto login;
		ObjectMapper mapper = new ObjectMapper();
		try {
			login = mapper.readValue(URLDecoder.decode(decyptedString, StandardCharsets.UTF_8.toString()),LoginRequestDto.class);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			log.error("Unable to unserialize the password field " + e.getMessage());
			throw new BadDataException(GatewayConstants.INVALID_ERROR);
		}
		String password = login.getPassword();
		validateLoginRequestParams(login.getCurrentTimestamp());

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
		if (latestTime > instant.plus(2, ChronoUnit.MINUTES).getEpochSecond() * 1000) {
			throw new BadDataException(GatewayConstants.REQUEST_TIMEOUT);
		}
	}	 
}
