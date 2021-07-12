package com.mirrorfly.gateway.service;

import java.net.URI;
import java.util.List;
import java.util.Random;

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
import com.mirrorfly.gateway.dto.TrialChatUserDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.UserRepo;
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
	private VcardSearchRepo vcardSearchRepo;

	private Random random = new Random();// Random Generate user name

	public Response<String> registerNewUser(SanboxUserRegisterDto registerDto) {
		SandboxRegisterResponseDto registerResponseDto = new SandboxRegisterResponseDto();
		String jsonString = null;
		registerResponseDto.setIsProfileUpdated(false);
		dataValidating(registerDto);// validate Empty value
		URI targetUrl = UriComponentsBuilder
				.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/license/validate/")
				.queryParam("licenseKey", registerDto.getLicenseKey()).build().toUri();
		try {
			restTemplate.getForObject(targetUrl, Response.class);
		} catch (HttpStatusCodeException e) {
			handlingExceptionOfApiCall(e);
		}
		ChatUser chatUser = chatUserRepo.findByUserIdentifier(registerDto.getUserIdentifier());
		if (chatUser != null) {
			if (!chatUser.getLicenseKey().equalsIgnoreCase(registerDto.getLicenseKey())) {
				throw new BadDataException(GatewayConstants.USER_USERIDENTIFIER_ALREADY_REGISTERED);
			}
			if (!registerDto.getDeviceType().equalsIgnoreCase("web")) {
				updateUserMobileDetails(registerDto, chatUser);
			}
			Users users = userRepo.findByUsername(chatUser.getUsername());
			registerResponseDto.setPassword(users.getPassword());
			registerResponseDto.setUsername(users.getUsername());
			registerResponseDto.setToken(tokenService.getToken(users.getUsername(), registerDto.getDeviceType(),
					registerDto.getDeviceType()));
			registerResponseDto.setIsExisting(true);
		} else {
			String username = random.ints(97, 122 + 1).limit(10)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
			registerResponseDto = registerUser(registerDto, username);
			registerResponseDto.setIsExisting(false);
			URI url = UriComponentsBuilder
					.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/license/addUserCount").build()
					.toUri();
			AddUserDto adduser = new AddUserDto();
			adduser.setLicenseKey(registerDto.getLicenseKey());
			TrialChatUserDto bean = setDataForDashboard(registerDto);// for dashboard chat data
			URI dashboardUrl = UriComponentsBuilder
					.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/dashboard/chatUser/").build()
					.toUri();
			try {
				restTemplate.postForObject(dashboardUrl, bean, Response.class);// Execute the Rest URL-
				restTemplate.postForObject(url, adduser, Response.class);
			} catch (HttpStatusCodeException e) {
				handlingExceptionOfApiCall(e);
			}
		}
		VcardSearch vcardDetails = vcardSearchRepo.findByUsername(registerResponseDto.getUsername());
		if (vcardDetails != null)
			registerResponseDto.setIsProfileUpdated(true);

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
		response.setMessage(GatewayConstants.CUSTOMER_CREATED);
		return response;
	}

	private TrialChatUserDto setDataForDashboard(SanboxUserRegisterDto registerDto) {
		TrialChatUserDto bean = new TrialChatUserDto();
		List<ChatUser> data = chatUserRepo.findBylicenseKey(registerDto.getLicenseKey());
		int ios = 0;
		int web = 0;
		int android = 0;
		if (data != null) {
			for (ChatUser value : data) {
				if (value.getDevicetype() != null) {
					if (value.getDevicetype().equalsIgnoreCase("web")) {
						web += 1;
					} else if (value.getDevicetype().equalsIgnoreCase("ios")) {
						ios += 1;
					} else {
						android += 1;
					}
				} else {
					web += 1;
				}
			}
			bean.setTotalActiveUserCount(data.size());
			bean.setActiveUserCount(Long.valueOf(data.size()));
			bean.setTotalUserCount(Long.valueOf(data.size()));
			bean.setNewWebUserCount(web);
			bean.setNewIosUserCount(ios);
			bean.setNewAndroidUserCount(android);
		} else {
			bean.setTotalActiveUserCount(1);
			bean.setActiveUserCount(Long.valueOf(1));
			bean.setTotalUserCount(Long.valueOf(1));
			if (registerDto.getDeviceType().equalsIgnoreCase("web")) {
				bean.setNewWebUserCount(1);
			} else if (registerDto.getDeviceType().equalsIgnoreCase("ios")) {
				bean.setNewIosUserCount(1);
			} else {
				bean.setNewAndroidUserCount(1);
			}
		}
		bean.setLicenseKey(registerDto.getLicenseKey());
		bean.setEnvironment(applicationProperties.getEnvironment());
		return bean;
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
		ChatUser chatUser = chatUserRepo.findByUsername(username);
		if (chatUser == null) {
			chatUser = new ChatUser();
			chatUser.setUsername(username);
			chatUser.setMobileNumber(registerDto.getMobileNumber());
			chatUser.setCreatedAt(GatewayUtil.getCurrentDateTime());
		}
		if (!registerDto.getDeviceType().equalsIgnoreCase("web")) {
			if (registerDto.getVoipDeviceToken() != null && !registerDto.getVoipDeviceToken().equals("")) {
				chatUser.setVoipDeviceToken(registerDto.getVoipDeviceToken());
			} else {
				chatUser.setVoipDeviceToken(registerDto.getDeviceToken());
			}
			if (registerDto.getDeviceOsVersion() != null && !registerDto.getDeviceOsVersion().equals("")) {
				chatUser.setDeviceOsVersion(registerDto.getDeviceOsVersion());
			} else {
				chatUser.setDeviceOsVersion("12.0");
			}
			chatUser.setDeviceToken(registerDto.getDeviceToken());
			chatUser.setDevicetype(registerDto.getDeviceType());
		}
		chatUser.setMode(registerDto.getMode());
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
		registerResponseDto.setIsExisting(false);
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
