package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.RegisterDto;
import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterResponseDto;
import com.mirrorfly.gateway.dto.UserCountRequestDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

@Service
public class RegisterService {

	@Autowired
	private ChatSettingsRepo settingsRepo;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private ChatUserRepo chatUserRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ApplicationProperties applicationProperties;
	
	@Autowired
	private RestTemplate restTemplate;

	public Response<RegisterInfoDto> getRegisterInfo() {
		ChatSettings settings = settingsRepo.findAll().get(0);
		RegisterInfoDto info = new RegisterInfoDto();
		info.setDomain(settings.getDomain());
		info.setMailLogin(settings.getMailLogin());
		Response<RegisterInfoDto> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setData(info);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public Response<String> registerNewUser(RegisterDto registerDto) {
		RegisterResponseDto registerResponseDto = registerUser(registerDto);
		ObjectMapper mapper = new ObjectMapper();
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(registerResponseDto);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}
		String licenseKey =applicationProperties.getLicenseKey();
		Response<String> response = new Response<>();
		response.setData(AESEncryption.encryptString(jsonString,licenseKey + licenseKey + licenseKey));
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.DATA_RETRIEVED);		
		return response;
	}

	private RegisterResponseDto registerUser(RegisterDto registerDto) {
		inputDataValidating(registerDto);
		ChatUser chatUser = chatUserRepo.findByUserIdentifier(registerDto.getUserIdentifier());
		if (chatUser == null) {
			UserCountRequestDto request = new UserCountRequestDto();
			request.setLicenseKey(registerDto.getLicenseKey());
			String url = applicationProperties.getWebportalApiBaseUrl() + "/license/addUserCount";
			try {
				restTemplate.postForObject(url, request, Response.class);
			} catch (HttpStatusCodeException e) {
				handlingExceptionOfApiCall(e);
			}
			chatUser = new ChatUser();
			chatUser.setUsername(registerDto.getUserIdentifier());
			chatUser.setMobileNumber(registerDto.getMobileNumber());
			chatUser.setCreatedAt(GatewayUtil.getCurrentDateTime());
			chatUser.setUserIdentifier(registerDto.getUserIdentifier());
		}
		if(!registerDto.getDeviceType().equalsIgnoreCase("web")) {
			if (registerDto.getDeviceToken() != null && !registerDto.getDeviceToken().trim().equals("")) {
				chatUserRepo.updateDeviceTokenAsEmpty(registerDto.getDeviceToken());
			}
			if (registerDto.getVoipDeviceToken() != null && !registerDto.getVoipDeviceToken().equals("")) {
				chatUser.setVoipDeviceToken(registerDto.getVoipDeviceToken());
			} else {
				chatUser.setVoipDeviceToken(registerDto.getDeviceToken());
			}
			if (registerDto.getDeviceOsVersion() != null && !registerDto.getDeviceOsVersion().equals("")) {
				chatUser.setDeviceOsVersion(registerDto.getDeviceOsVersion());
			} else {
				if(registerDto.getDeviceType().equalsIgnoreCase("ios")) {
					chatUser.setDeviceOsVersion("12.0");
				} else {
					chatUser.setDeviceOsVersion("");
				}
			}
			chatUser.setDeviceToken(registerDto.getDeviceToken());
			chatUser.setDevicetype(registerDto.getDeviceType());
			chatUser.setMode(registerDto.getMode());
		}
		chatUserRepo.save(chatUser);
		RegisterResponseDto registerResponseDto = new RegisterResponseDto();
		registerResponseDto.setPassword(passwordService.generateRandomPassword());
		registerResponseDto.setToken(tokenService.getToken(registerDto.getUserIdentifier(), registerDto.getDeviceType(),
				registerDto.getDeviceType()));
		registerResponseDto.setUsername(registerDto.getUserIdentifier());
		Users users = new Users();
		users.setPassword(registerResponseDto.getPassword());
		users.setUsername(registerDto.getUserIdentifier());
		users.setWsPassword(registerResponseDto.getPassword());
		userRepo.save(users);
		return registerResponseDto;
	}
	
	private void inputDataValidating(RegisterDto registerDto) {
		if (registerDto.getUserIdentifier() == null || registerDto.getUserIdentifier().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_IDENTIFIER_EMPTY);
		}
		if(registerDto.getUserIdentifier().contains("\"")) {
			throw new BadDataException("User identifier should not contain double quotes.");
		}
		if(registerDto.getUserIdentifier().contains("&")) {
			throw new BadDataException("User identifier should not contain ampersand.");
		}
		if(registerDto.getUserIdentifier().contains("\'")) {
			throw new BadDataException("User identifier should not contain back slash.");
		}
		if(registerDto.getUserIdentifier().contains("/")) {
			throw new BadDataException("User identifier should not contain forward slash.");
		}
		if(registerDto.getUserIdentifier().contains(":")) {
			throw new BadDataException("User identifier should not contain colon.");
		}
		if(registerDto.getUserIdentifier().contains("<")) {
			throw new BadDataException("User identifier should not contain lesser-than.");
		}
		if(registerDto.getUserIdentifier().contains(">")) {
			throw new BadDataException("User identifier should not contain greater-than.");
		}
		if(registerDto.getUserIdentifier().contains("@")) {
			throw new BadDataException("User identifier should not contain  at sign.");
		}
		if (registerDto.getLicenseKey() == null || registerDto.getLicenseKey().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_LICENSEKEY_EMPTY);
		}
		if (registerDto.getDeviceType() == null || registerDto.getDeviceType().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.DEVICE_TYPE_EMPTY);
		}
		if (!registerDto.getDeviceType().equalsIgnoreCase("android") && !registerDto.getDeviceType().equalsIgnoreCase("web") && !registerDto.getDeviceType().equalsIgnoreCase("ios")) {
			throw new BadDataException(GatewayConstants.DEVICE_TYPE_NOT_VALID);
		}
	}

	private void handlingExceptionOfApiCall(HttpStatusCodeException e) {
		String responseBody = e.getResponseBodyAsString();
		ObjectMapper mapper = new ObjectMapper();
		try {
			Response<?> r = mapper.readValue(responseBody, Response.class);
			throw new BadDataException(r.getMessage());
		} catch (JsonProcessingException ex) {
			throw new BadDataException("Error in registration. Please try again later");
		}
	}

}
