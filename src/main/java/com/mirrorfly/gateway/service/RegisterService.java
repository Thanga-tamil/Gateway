package com.mirrorfly.gateway.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.RegisterDto;
import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterRequestDto;
import com.mirrorfly.gateway.dto.RegisterResponseDto;
import com.mirrorfly.gateway.dto.SanboxUserRegisterDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.ChatUserDetails;
import com.mirrorfly.gateway.model.Last;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.model.Vcard;
import com.mirrorfly.gateway.model.VcardSearch;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.ChatUsersDetailsRepo;
import com.mirrorfly.gateway.repo.LastRepo;
import com.mirrorfly.gateway.repo.SpoolRepo;
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

	@Autowired
	private LastRepo lastRepo;

	@Autowired
	private ConfigService configService;

	@Autowired
	private VcardService vcardService;

	@Autowired
	private ChatUsersDetailsRepo chatUsersDetailsRepo;

	@Autowired
	private SpoolRepo spoolRepo;
	
	@Autowired
	private GatewayUtil gatewayUtil;

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

	public Response<String> registerNewUser(RegisterRequestDto registerRequestDto) {
		String decyptedString = AESEncryption.decryptString(registerRequestDto.getRegisterData(),
				applicationProperties.getLicenseKey() + applicationProperties.getLicenseKey()
						+ applicationProperties.getLicenseKey());
		if (decyptedString.equals("")) {
			throw new BadDataException(GatewayConstants.LICENSEKEY_INVALID);
		}
		ObjectMapper decryptmapper = new ObjectMapper();
		RegisterDto registerDto;
		try {
			registerDto = decryptmapper.readValue(URLDecoder.decode(decyptedString, StandardCharsets.UTF_8.toString()),
					RegisterDto.class);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			throw new BadDataException(GatewayConstants.INVALID_ERROR);
		}
		RegisterResponseDto registerResponseDto = registerUser(registerDto);
		// get config details and return in the response
		String licenseKey = applicationProperties.getLicenseKey();
		registerResponseDto.setConfig(configService.getConfigData(licenseKey));
		ObjectMapper mapper = new ObjectMapper();
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(registerResponseDto);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}
		Response<String> response = new Response<>();
		response.setData(AESEncryption.encryptString(jsonString, licenseKey + licenseKey + licenseKey));
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public void registerLogintime(String username) {
		Last last = new Last();
		last.setUsername(username);
		last.setStatus(0);
		LocalDateTime date = LocalDateTime.now();
		ZoneOffset zone = ZoneOffset.of("Z");
		last.setSeconds(Long.toString(date.toEpochSecond(zone)));
		last.setPresence(0);
		last.setState("Stream reset by peer");
		lastRepo.save(last);
	}

	private RegisterResponseDto registerUser(RegisterDto registerDto) {
		RegisterResponseDto registerResponseDto = new RegisterResponseDto();

		inputDataValidating(registerDto);

		Users user = new Users();

		ChatUser chatUser = chatUserRepo.findByUserIdentifier(registerDto.getUserIdentifier());
		if (chatUser == null) {
			String url = applicationProperties.getWebportalApiBaseUrl() + "/license/validate";
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("licenseKey",
						registerDto.getLicenseKey());
				HttpEntity<?> entity = new HttpEntity<>(headers);
				restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
			} catch (HttpStatusCodeException e) {
				handlingExceptionOfApiCall(e);
			}
			chatUser = new ChatUser();
			chatUser.setUsername(registerDto.getUserIdentifier());
			chatUser.setMobileNumber(registerDto.getMobileNumber());
			chatUser.setCreatedAt(GatewayUtil.getCurrentDateTime());
			chatUser.setUserIdentifier(registerDto.getUserIdentifier());
			chatUser.setLicenseKey(registerDto.getLicenseKey());
			chatUser.setIsDeleteInprogrees(0);

			// To insert the register login time
			registerLogintime(registerDto.getUserIdentifier());
			user.setUsername(registerDto.getUserIdentifier());
			// Insert the vcard details
			VcardSearch vcardSearch = vcardService.updateVcardSearchDetails(registerDto.getUserIdentifier());
			vcardService.updateVcard(vcardSearch, new Vcard());
			registerResponseDto.setIsProfileUpdated(false);
			registerResponseDto.setIsExisting(false);
		} else {
			user = userRepo.findByUsername(chatUser.getUsername());
			if (chatUser.getIsDeleteInprogrees() == 1) {
				throw new BadDataException(GatewayConstants.DELETE_INPROGRESS);
			}
			VcardSearch vcardSearch = vcardService.findVcardSearchByUserName(chatUser.getUsername());
			if (vcardSearch == null) {
				vcardSearch = vcardService.updateVcardSearchDetails(registerDto.getUserIdentifier());
				vcardService.updateVcard(vcardSearch, new Vcard());
				registerResponseDto.setIsProfileUpdated(false);
			} else {
				if (vcardSearch.getIsBlock() == 1) {
					throw new ForbiddenException(GatewayConstants.BLOCK_USER);
				}
				registerResponseDto.setIsProfileUpdated(true);
			}
			registerResponseDto.setIsExisting(true);

		}

		String metadata = GatewayUtil.convertMetaDataAsJson(registerDto.getMetaData());
		chatUser.setMetaData(metadata);

		chatUserRepo.save(chatUser);
		String password = updateChatUserDetails(registerDto, chatUser);
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
		registerResponseDto.setToken(tokenService.getToken(registerDto.getUserIdentifier(), deviceId, ""));
		registerResponseDto.setUsername(user.getUsername());
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.WEB)) {
			user.setWsPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		} else {
			user.setPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		}
		userRepo.save(user);
		return registerResponseDto;
	}

	public String updateChatUserDetails(SanboxUserRegisterDto registerDto, ChatUser chatUser) {
		RegisterDto data = new RegisterDto();
		data.setDeviceId(registerDto.getDeviceId());
		data.setDeviceOsVersion(registerDto.getDeviceOsVersion());
		data.setDeviceToken(registerDto.getDeviceToken());
		data.setDeviceType(registerDto.getDeviceType());
		data.setLicenseKey(registerDto.getLicenseKey());
		data.setMobileNumber(registerDto.getMobileNumber());
		data.setMode(registerDto.getMode());
		data.setUserIdentifier(registerDto.getUserIdentifier());
		data.setVoipDeviceToken(registerDto.getVoipDeviceToken());
		data.setForceRegister(registerDto.getForceRegister());
		data.setPushType(registerDto.getPushType());
		return updateChatUserDetails(data, chatUser);
	}

	public String updateChatUserDetails(RegisterDto registerDto, ChatUser chatUser) {
		String deviceIdprefix = "";
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB)) {
			deviceIdprefix = "-web";
		} else {
			deviceIdprefix = "-mobile";
			if (registerDto.getDeviceToken() != null && !registerDto.getDeviceToken().isEmpty()) {
				chatUsersDetailsRepo.updateDeviceTokenAsEmpty(registerDto.getDeviceToken());
			}
			if (registerDto.getVoipDeviceToken() != null && !registerDto.getVoipDeviceToken().isEmpty()) {
				chatUsersDetailsRepo.updateVoipDeviceTokenAsEmpty(registerDto.getVoipDeviceToken());
			}
			if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_IOS)
					&& (registerDto.getDeviceOsVersion() == null || registerDto.getDeviceOsVersion().equals(""))) {
				registerDto.setDeviceOsVersion("12.0");
			}
		}
		String deviceId = registerDto.getDeviceId() != null && !registerDto.getDeviceId().isEmpty()
				? registerDto.getDeviceId()
				: "MF-01" + deviceIdprefix;
		ChatUserDetails user = chatUsersDetailsRepo.findByUsernameAndDeviceId(chatUser.getUsername(), deviceId);
		String password = passwordService.generateRandomPassword();
		if (user == null) {
			List<ChatUserDetails> existingChatUsersDetails = chatUsersDetailsRepo
					.findByUsername(chatUser.getUsername());
			if (existingChatUsersDetails.size() >= applicationProperties.getMaximumSessions()) {
				ChatUserDetails deletableRecord = findDeletableChatUsersDetails(existingChatUsersDetails,
						registerDto.getDeviceType());
				if (deletableRecord != null) {
					chatUsersDetailsRepo.delete(deletableRecord);
					spoolRepo.deleteOfflilneRecordsByUsernameAndDeviceId(deletableRecord.getUsername(),
							deletableRecord.getDeviceId());
				}
			}
			user = new ChatUserDetails();
			user.setCreatedAt(LocalDateTime.now());
			user.setDeviceId(deviceId);
			user.setDeviceOsVersion(registerDto.getDeviceOsVersion());
			user.setDeviceToken(registerDto.getDeviceToken());
			user.setDeviceType(registerDto.getDeviceType());
			user.setLastLoggedInAt(LocalDateTime.now());
			user.setMode(registerDto.getMode());
			user.setPassword(GatewayUtil.getMd5String(password));
			user.setUsername(chatUser.getUsername());
			user.setVoipDeviceToken(registerDto.getVoipDeviceToken());
			user.setPushType(GatewayConstants.PUSH_TYPE_DEFAULT);
			if(StringUtils.isNotBlank(registerDto.getPushType())) {
				user.setPushType(registerDto.getPushType().trim());
			}
			chatUsersDetailsRepo.save(user);
			return password;
		} else {
			user.setDeviceOsVersion(registerDto.getDeviceOsVersion());
			user.setDeviceToken(registerDto.getDeviceToken());
			user.setDeviceType(registerDto.getDeviceType());
			user.setLastLoggedInAt(LocalDateTime.now());
			user.setMode(registerDto.getMode());
			user.setPassword(GatewayUtil.getMd5String(password));
			user.setVoipDeviceToken(registerDto.getVoipDeviceToken());
			if(StringUtils.isNotBlank(registerDto.getPushType())) {
				user.setPushType(registerDto.getPushType().trim());
			}
			chatUsersDetailsRepo.save(user);
			return password;
		}
	}

	private ChatUserDetails findDeletableChatUsersDetails(List<ChatUserDetails> existingChatUsersDetails,
			String deviceType) {
		if (deviceType.equalsIgnoreCase(GatewayConstants.DEVICE_WEB)) {
			List<ChatUserDetails> samePlatformDevices = existingChatUsersDetails.stream()
					.filter(p -> p.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB))
					.sorted(Comparator.comparing(ChatUserDetails::getLastLoggedInAt)).collect(Collectors.toList());
			if (!samePlatformDevices.isEmpty()) {
				// need to delete first logged in web device
				return samePlatformDevices.get(0);
			}
		}
		// need to delete first logged in record irrespective to platform
		Collections.sort(existingChatUsersDetails, Comparator.comparing(ChatUserDetails::getLastLoggedInAt));
		return existingChatUsersDetails.get(0);
	}

	private void inputDataValidating(RegisterDto registerDto) {
		String userIdentifier = registerDto.getUserIdentifier();
		if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_IDENTIFIER_EMPTY);
		}
		if (userIdentifier.contains("\"") || userIdentifier.contains("&") || userIdentifier.contains("\'")
				|| userIdentifier.contains("/") || userIdentifier.contains(":") || userIdentifier.contains("<")
				|| userIdentifier.contains(">") || userIdentifier.contains("@")) {
			throw new BadDataException("User identifier should not contain (\",&,\',/,:,<,>,@).");
		}
		setSandBoxUserAndValidate(registerDto);
	}
	
	private void setSandBoxUserAndValidate(RegisterDto registerDto) {
		SanboxUserRegisterDto sandbox = new SanboxUserRegisterDto();
		sandbox.setUserIdentifier(registerDto.getUserIdentifier());
		sandbox.setPushType(registerDto.getPushType());
		sandbox.setDeviceType(registerDto.getDeviceType());
		sandbox.setMetaData(registerDto.getMetaData());
		sandbox.setForceRegister(registerDto.getForceRegister());
		sandbox.setLicenseKey(registerDto.getLicenseKey());
		gatewayUtil.dataValidating(sandbox);
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
