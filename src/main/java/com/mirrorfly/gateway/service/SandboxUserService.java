package com.mirrorfly.gateway.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.AddUserDto;
import com.mirrorfly.gateway.dto.SanboxUserRegisterDto;
import com.mirrorfly.gateway.dto.SandboxRegisterResponseDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.model.Vcard;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.repo.VcardRepo;
import com.mirrorfly.gateway.repo.VcardSearch;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

@Service
public class SandboxUserService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private ChatUserRepo chatUserRepo;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private VcardRepo vcardRepo;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;
	
	public Response<String> registerNewUser(SanboxUserRegisterDto registerDto) {
		SandboxRegisterResponseDto registerResponseDto = new SandboxRegisterResponseDto();
		String jsonString = null;
		dataValidating(registerDto);// validate Empty value
		URI targetUrl = UriComponentsBuilder
				.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/license/validate")
				.queryParam("licenseKey", registerDto.getLicenseKey()).build().toUri();
		try {
			restTemplate.getForObject(targetUrl, Response.class);
		} catch (HttpStatusCodeException e) {
			handlingExceptionOfApiCall(e);
		}
		ChatUser chatUser = chatUserRepo.findByUserIdentifier(registerDto.getUserIdentifier());
		if (chatUser != null) {
			if(chatUser.getIsDeleteInprogrees() == 1) {
				throw new BadDataException(GatewayConstants.DELETE_INPROGRESS);
			}	
			VcardSearch vcardSearch = vcardSearchRepo.findByUsername(chatUser.getUsername());
			if (vcardSearch != null && vcardSearch.getIsBlock() == 1) { 
				throw new ForbiddenException(GatewayConstants.BLOCK_USER);
 			}
			if (!chatUser.getLicenseKey().equalsIgnoreCase(registerDto.getLicenseKey())) {
				throw new BadDataException(GatewayConstants.USER_USERIDENTIFIER_ALREADY_REGISTERED);
			}
			if (!registerDto.getDeviceType().equalsIgnoreCase("web")) {
				if (registerDto.getDeviceToken() != null && !registerDto.getDeviceToken().trim().equals("")) {
					chatUserRepo.updateDeviceTokenAsEmpty(registerDto.getDeviceToken());
				}
				updateUserMobileDetails(registerDto, chatUser);
			}
			Users users = userRepo.findByUsername(chatUser.getUsername());
			registerResponseDto.setPassword(users.getPassword());
			registerResponseDto.setUsername(users.getUsername());
			registerResponseDto.setToken(tokenService.getToken(users.getUsername(), registerDto.getDeviceType(),
					registerDto.getDeviceType()));
			registerResponseDto.setIsExisting(true);
		} else {
			String username = GatewayUtil.getAlphaNumericString(10).toLowerCase();
			registerResponseDto = registerUser(registerDto, username);
			registerResponseDto.setIsExisting(false);
			URI url = UriComponentsBuilder
					.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/license/addUserCount").build()
					.toUri();
			AddUserDto adduser = new AddUserDto();
			adduser.setLicenseKey(registerDto.getLicenseKey());
			try {
				restTemplate.postForObject(url, adduser, Response.class);
			} catch (HttpStatusCodeException e) {
				handlingExceptionOfApiCall(e);
			}
		}
		Vcard vcards = vcardRepo.findByUsername(registerResponseDto.getUsername());
		if(vcards != null) {
			registerResponseDto.setIsProfileUpdated(true);   
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonString = mapper.writeValueAsString(registerResponseDto);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}
		Response<String> response = new Response<>();
		response.setData(AESEncryption.encryptString(jsonString,
				registerDto.getLicenseKey() + registerDto.getLicenseKey() + registerDto.getLicenseKey()));
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.USER_ADD_SUCCESS);
		return response;
	}

	private void dataValidating(SanboxUserRegisterDto registerDto) {
		if (registerDto.getUserIdentifier() == null || registerDto.getUserIdentifier().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_IDENTIFIER_EMPTY);
		}
		if (registerDto.getLicenseKey() == null || registerDto.getLicenseKey().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_LICENSEKEY_EMPTY);
		}
		if (registerDto.getDeviceType() == null || registerDto.getDeviceType().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.DEVICE_TYPE_EMPTY);
		}
		if (!registerDto.getDeviceType().equalsIgnoreCase("android")
				&& !registerDto.getDeviceType().equalsIgnoreCase("web")
				&& !registerDto.getDeviceType().equalsIgnoreCase("ios")) {
			throw new BadDataException(GatewayConstants.DEVICE_TYPE_NOT_VALID);
		}
	}

	private void updateUserMobileDetails(SanboxUserRegisterDto registerDto, ChatUser chatUser) {
		if (registerDto.getDeviceType().equalsIgnoreCase("ios")) {
			chatUser.setVoipDeviceToken(registerDto.getVoipDeviceToken());
			chatUser.setDeviceToken(registerDto.getDeviceToken());
			if (registerDto.getDeviceOsVersion() != null && !registerDto.getDeviceOsVersion().equals("")) {
				chatUser.setDeviceOsVersion(registerDto.getDeviceOsVersion());
			} else {
				chatUser.setDeviceOsVersion("12.0");
			}
		} else if (registerDto.getDeviceType().equalsIgnoreCase("android")) {
			chatUser.setDeviceToken(registerDto.getDeviceToken());
			chatUser.setDeviceOsVersion(registerDto.getDeviceOsVersion());
		} else {
			throw new BadDataException("Invaild device type");
		}
		chatUser.setDevicetype(registerDto.getDeviceType());
		chatUser.setMode(registerDto.getMode());
		chatUserRepo.save(chatUser);
	}

	private SandboxRegisterResponseDto registerUser(SanboxUserRegisterDto registerDto, String username) {
		ChatUser chatUser = new ChatUser();
		chatUser.setUsername(username);
		chatUser.setMobileNumber(registerDto.getMobileNumber());
		chatUser.setCreatedAt(GatewayUtil.getCurrentDateTime());
		if (!registerDto.getDeviceType().equalsIgnoreCase("web")) {
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
			} else if ((registerDto.getDeviceOsVersion() == null || registerDto.getDeviceOsVersion().equals(""))
					&& registerDto.getDeviceType().equalsIgnoreCase("ios")) {
				chatUser.setDeviceOsVersion("12.0");
			} else {
				chatUser.setDeviceOsVersion("");
			}
			chatUser.setDeviceToken(registerDto.getDeviceToken());
			chatUser.setMode(registerDto.getMode());
		}
		chatUser.setDevicetype(registerDto.getDeviceType());
		chatUser.setLicenseKey(registerDto.getLicenseKey());
		chatUser.setUserIdentifier(registerDto.getUserIdentifier());
		chatUserRepo.save(chatUser);

		SandboxRegisterResponseDto registerResponseDto = new SandboxRegisterResponseDto();
		registerResponseDto.setPassword(passwordService.generateRandomPassword());
		registerResponseDto
				.setToken(tokenService.getToken(username, registerDto.getDeviceType(), registerDto.getDeviceType()));
		registerResponseDto.setUsername(username);

		Users users = new Users();
		users.setPassword(registerResponseDto.getPassword());
		users.setUsername(username);
		userRepo.save(users);

		return registerResponseDto;
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
