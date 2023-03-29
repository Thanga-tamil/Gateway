package com.mirrorfly.gateway.service;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.AddUserDto;
import com.mirrorfly.gateway.dto.LicenseValidateResponseDto;
import com.mirrorfly.gateway.dto.SanboxUserRegisterDto;
import com.mirrorfly.gateway.dto.SandboxRegisterResponseDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.ChatUserDetails;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.model.Vcard;
import com.mirrorfly.gateway.model.VcardSearch;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.ChatUsersDetailsRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
	private UserRepo userRepo;

	@Autowired
	private RegisterService registerService;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	@Autowired
	private ConfigService configService;

	@Autowired
	private VcardService vcardService;

	@Autowired
	private ChatUsersDetailsRepo chatUsersDetailsRepo;

	public Response<String> registerNewUser(SanboxUserRegisterDto registerDto) throws JsonProcessingException {
		SandboxRegisterResponseDto registerResponseDto = new SandboxRegisterResponseDto();
		String jsonString = null;
		dataValidating(registerDto);
		Users users = new Users();
		ChatUser chatUser = chatUserRepo.findByUserIdentifier(registerDto.getUserIdentifier());
		if (chatUser != null) {
			if (chatUser.getIsDeleteInprogrees() == 1) {
				throw new BadDataException(GatewayConstants.DELETE_INPROGRESS);
			}
			VcardSearch vcardSearch = vcardSearchRepo.findByUsername(chatUser.getUsername());
			if (vcardSearch != null && vcardSearch.getIsBlock() == 1) {
				throw new ForbiddenException(GatewayConstants.BLOCK_USER);
			}
			if (!chatUser.getLicenseKey().equalsIgnoreCase(registerDto.getLicenseKey())) {
				throw new BadDataException(GatewayConstants.USER_USERIDENTIFIER_ALREADY_REGISTERED);
			}
			users = userRepo.findByUsername(chatUser.getUsername());
			registerResponseDto.setUsername(chatUser.getUsername());
			registerResponseDto.setIsExisting(true);
			if (vcardSearch == null) {
				vcardSearch = vcardService.updateVcardSearchDetails(registerDto.getUserIdentifier());
				vcardService.updateVcard(vcardSearch, new Vcard());
				registerResponseDto.setIsProfileUpdated(false);
			} else {
				registerResponseDto.setIsProfileUpdated(true);
			}
		} else {
			URI targetUrl = UriComponentsBuilder
					.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/license/validate")
					.queryParam("licenseKey", registerDto.getLicenseKey()).build().toUri();

			URI url = UriComponentsBuilder
					.fromUriString(applicationProperties.getWebportalApiBaseUrl() + "/license/addUserCount").build()
					.toUri();
			registerService.registerLogintime(registerDto.getUserIdentifier());// New entry in last login time
			AddUserDto adduser = new AddUserDto();
			adduser.setLicenseKey(registerDto.getLicenseKey());
			try {
				String apiResponse = restTemplate.getForObject(targetUrl, String.class);
				ObjectMapper objectMapper = new ObjectMapper();
				Response<LicenseValidateResponseDto> result = objectMapper.readValue(apiResponse,
						new TypeReference<Response<LicenseValidateResponseDto>>() {
						});
				if (result != null && result.getStatus() != null && result.getData() != null
						&& result.getStatus() == 200) {
					if (Boolean.TRUE.equals(result.getData().getIsFreemiumUser())) {
						validateUserCount(result.getData().getMaxUsersCount(), registerDto.getLicenseKey());
					}
				} else {
					log.error("Error in validating the license key => " + result);
					throw new BadDataException(GatewayConstants.ERROR_IN_REGISTRATION);
				}
				restTemplate.postForObject(url, adduser, Response.class);
			} catch (HttpStatusCodeException e) {
				handlingExceptionOfApiCall(e);
			}
			String username = registerDto.getUserIdentifier();
			// Create new user
			chatUser = registerUser(registerDto, username);
			registerResponseDto.setUsername(username);
			registerResponseDto.setIsExisting(false);
			users.setUsername(username);
			// Insert the vcard details
			VcardSearch vcardSearch = vcardService.updateVcardSearchDetails(username);
			vcardService.updateVcard(vcardSearch, new Vcard());
			registerResponseDto.setIsProfileUpdated(false);
		}
		String password = registerService.updateChatUserDetails(registerDto, chatUser);
		registerResponseDto.setPassword(password);
		String deviceIdprefix = "";
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB)) {
			deviceIdprefix = "-web";
		} else {
			deviceIdprefix = "-mobile";
		}
		String deviceId = registerDto.getDeviceId() != null && !registerDto.getDeviceId().isEmpty()
				? registerDto.getDeviceId()
				: "MF-01" + deviceIdprefix;
		registerResponseDto.setToken(tokenService.getToken(registerResponseDto.getUsername(), deviceId, ""));
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.WEB)) {
			users.setWsPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		} else {
			users.setPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		}
		userRepo.save(users);
		// get config details and return in the response
		String licenseKey = registerDto.getLicenseKey();
		registerResponseDto.setConfig(configService.getConfigData(licenseKey));
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

	private void validateUserCount(Long maxUserCount, String licenseKey) {
		Long count = chatUserRepo.getCountByLicenseKey(licenseKey);
		if (count >= maxUserCount) {
			throw new BadDataException(GatewayConstants.USER_COUNT_EXCEEDED);
		}
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
		List<ChatUserDetails> existingChatUsersDetails = chatUsersDetailsRepo
				.findByUsername(registerDto.getUserIdentifier());
		if (existingChatUsersDetails.size() >= applicationProperties.getMaximumSessions()
				&& Boolean.FALSE.equals(registerDto.getForceRegister())) {
			throw new ForbiddenException(GatewayConstants.USER_REGISTRATION_MAX_SESSIONS_REACHED);
		}
	}

	private ChatUser registerUser(SanboxUserRegisterDto registerDto, String username) {
		ChatUser chatUser = new ChatUser();
		chatUser.setUsername(username);
		chatUser.setMobileNumber(registerDto.getMobileNumber());
		chatUser.setCreatedAt(GatewayUtil.getCurrentDateTime());
		chatUser.setLicenseKey(registerDto.getLicenseKey());
		chatUser.setUserIdentifier(registerDto.getUserIdentifier());
		chatUser.setIsDeleteInprogrees(0);
		chatUserRepo.save(chatUser);
		return chatUser;
	}

	private void handlingExceptionOfApiCall(HttpStatusCodeException e) {
		String responseBody = e.getResponseBodyAsString();
		ObjectMapper mapper = new ObjectMapper();
		try {
			Response<?> r = mapper.readValue(responseBody, Response.class);
			throw new BadDataException(r.getMessage());
		} catch (JsonProcessingException ex) {
			throw new BadDataException(GatewayConstants.ERROR_IN_REGISTRATION);
		}
	}

}
