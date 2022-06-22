package com.mirrorfly.gateway.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Last;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.model.Vcard;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.LastRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.repo.VcardRepo;
import com.mirrorfly.gateway.repo.VcardSearch;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
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
	private VcardRepo vcardRepo;

	@Autowired
	private LastRepo lastRepo;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	@Autowired
	private ConfigService configService;

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
		Vcard vcards = vcardRepo.findByUsername(registerResponseDto.getUsername());
		if (vcards != null) {
			registerResponseDto.setIsProfileUpdated(true);
		}
		// get confiq details and return in the response
		registerResponseDto.setConfig(configService.getCongigData());
		ObjectMapper mapper = new ObjectMapper();
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(registerResponseDto);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}
		String licenseKey = applicationProperties.getLicenseKey();
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
			// To insert the register login time
			registerLogintime(registerDto.getUserIdentifier());
			user.setUsername(registerDto.getUserIdentifier());
		} else {
			user = userRepo.findByUsername(chatUser.getUsername());
			if (chatUser.getIsDeleteInprogrees() == 1) {
				throw new BadDataException(GatewayConstants.DELETE_INPROGRESS);
			}
			VcardSearch vcardSearch = vcardSearchRepo.findByUsername(chatUser.getUsername());
			if (vcardSearch != null && vcardSearch.getIsBlock() == 1) {
				throw new ForbiddenException(GatewayConstants.BLOCK_USER);
			}
		}
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
			} else {
				if (registerDto.getDeviceType().equalsIgnoreCase("ios")) {
					chatUser.setDeviceOsVersion("12.0");
				} else {
					chatUser.setDeviceOsVersion("");
				}
			}
			chatUser.setDeviceToken(registerDto.getDeviceToken());
			chatUser.setDevicetype(registerDto.getDeviceType());
			chatUser.setMode(registerDto.getMode());
		}
		chatUser.setIsDeleteInprogrees(0);
		chatUserRepo.save(chatUser);
		RegisterResponseDto registerResponseDto = new RegisterResponseDto();
		registerResponseDto.setPassword(passwordService.generateRandomPassword());
		registerResponseDto.setToken(tokenService.getToken(registerDto.getUserIdentifier(), registerDto.getDeviceType(),
				registerDto.getDeviceType()));
		registerResponseDto.setUsername(registerDto.getUserIdentifier());
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.WEB)) {
			user.setWsPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		} else {
			user.setPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		}		
		userRepo.save(user);
		return registerResponseDto;
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
		inputDataValidation(registerDto);
	}

	private void inputDataValidation(RegisterDto registerDto) {
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
