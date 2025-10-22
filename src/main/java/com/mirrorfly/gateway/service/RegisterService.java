package com.mirrorfly.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.dto.*;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.ForbiddenException;
import com.mirrorfly.gateway.model.*;
import com.mirrorfly.gateway.queue.QueueService;
import com.mirrorfly.gateway.repo.*;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.DataHelperUtils;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

	private final ChatSettingsRepo settingsRepo;

	private final TokenService tokenService;

	private final PasswordService passwordService;

	private final ChatUserRepo chatUserRepo;

	private final UserRepo userRepo;

	private final ApplicationProperties applicationProperties;

	private final RestTemplate restTemplate;

	private final LastRepo lastRepo;

	private final ConfigService configService;

	private final VcardService vcardService;

	private final ChatUsersDetailsRepo chatUsersDetailsRepo;

	private final DataStore dataStore;

	private final QueueService queueService;

    private final AsyncService asyncService;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@SwitchDataSource(value = "[0]")
	public Response<RegisterInfoDto> getRegisterInfo(String userdomain) {
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
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

	@SwitchDataSource(value = "[0]")
	public Response<String> registerNewUser(String userdomain, RegisterRequestDto registerRequestDto) {

        String licenseKey       = Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "licenseKey")).orElseThrow(() -> new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL));
		String xmppDomain       = Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "xmppDomain")).orElseThrow(() -> new BadDataException("Xmpp Domain not found"));
		String decyptedString   = Optional.ofNullable(AESEncryption.decryptString(registerRequestDto.getRegisterData(), licenseKey.repeat(3))).filter(s -> !s.isBlank()).orElseThrow(() -> new BadDataException(GatewayConstants.LICENSEKEY_INVALID));

		RegisterDto registerDto;

		try {
			registerDto = MAPPER.readValue(URLDecoder.decode(decyptedString, StandardCharsets.UTF_8), RegisterDto.class);
		} catch (JsonProcessingException e) {
            log.error("Register Payload Are Not Readable :: {}", e.getMessage());
			throw new BadDataException(GatewayConstants.INVALID_ERROR);
		}

		String contactNo = registerDto.getMobileNumber();

		registerDto.setMobileNumber(registerDto.getCountryCode() + contactNo);

		RegisterResponseDto registerResponseDto = registerUser(userdomain, registerDto);

		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

        registerResponseDto.setConfig(configService.getConfigData(licenseKey));

		String jsonString;

		try {
			jsonString = MAPPER.writeValueAsString(registerResponseDto);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}

        asyncService.updateRosters(userdomain, registerResponseDto.getUsername(), contactNo, xmppDomain);
        String encryptedString = AESEncryption.encryptString(jsonString, licenseKey.repeat(3));
        log.info("register response : "+ registerResponseDto.toString());
        log.info("register response encrypted value: "+ encryptedString);
        return new Response<>(HttpStatus.OK.value(), encryptedString, GatewayConstants.DATA_RETRIEVED);

	}

	public void registerLoginTime(String username) {
		Last last = new Last();
		last.setUsername(username);
		last.setStatus(0);
		LocalDateTime date = LocalDateTime.now();
		ZoneOffset zone = ZoneOffset.of("Z");
		last.setSeconds(BigInteger.valueOf(date.toEpochSecond(zone)));
		last.setPresence(0);
		last.setState("Stream reset by peer");
		lastRepo.save(last);
	}

	@SwitchDataSource(value = "[0]")
	private RegisterResponseDto registerUser(String userdomain, RegisterDto registerDto) {
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
        RegisterResponseDto registerResponseDto = new RegisterResponseDto();
        inputDataValidation(userdomain, registerDto);
        Users user = new Users();
		ChatUser chatUser = chatUserRepo.findByUserIdentifier(registerDto.getUserIdentifier());
		if (chatUser == null) {
			String url = applicationProperties.getWebportalApiBaseUrl() + "/license/validate";
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("licenseKey", registerDto.getLicenseKey());
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
			chatUser.setDevicetype(registerDto.getDeviceType());
			// To insert the register login time
            registerLoginTime(registerDto.getUserIdentifier());
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

		registerResponseDto.setPassword(updateChatUserDetails(userdomain, registerDto, chatUser));

		String deviceIdPrefix;
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB)) {
            deviceIdPrefix = "-web";
		} else {
            deviceIdPrefix = "-mobile";
		}
		String deviceId = StringUtils.isNotBlank(registerDto.getDeviceId()) ? registerDto.getDeviceId() : "MF-01" + deviceIdPrefix;

		registerResponseDto.setToken(tokenService.getToken(userdomain, registerDto.getUserIdentifier(), deviceId, ""));
		registerResponseDto.setUsername(user.getUsername());
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.WEB)) {
			user.setWsPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		} else {
			user.setPassword(GatewayUtil.getMd5String(registerResponseDto.getPassword()));
		}
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain); // Again setup the DB based on the domain
		userRepo.save(user);
		return registerResponseDto;
	}

	@SwitchDataSource(value = "[0]")
	public String updateChatUserDetails(String userdomain, SanboxUserRegisterDto registerDto, ChatUser chatUser) {
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
		data.setPushServerType(registerDto.getPushServerType());
		data.setUserType(registerDto.getUserType());
		return updateChatUserDetails(userdomain, data, chatUser);
	}

	public String updateChatUserDetails(String userdomain, RegisterDto registerDto, ChatUser chatUser) {
		String deviceIdprefix = "";
		String pushServerType;
		if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB)) {
            pushServerType = "";
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
			if (registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_ANDROID)) {
				pushServerType = "fcm";
			} else {
				if (registerDto.getPushServerType() != null && !registerDto.getPushServerType().trim().isEmpty()) {
					pushServerType = registerDto.getPushServerType();
				} else {
					pushServerType = "apns";
				}
			}
		}
		String deviceId = registerDto.getDeviceId() != null && !registerDto.getDeviceId().isEmpty() ? registerDto.getDeviceId() : "MF-01" + deviceIdprefix;

        String password = passwordService.generateRandomPassword();

        ChatUserDetails chatUserDetails = Optional.ofNullable(chatUsersDetailsRepo.findByUsernameAndDeviceId(chatUser.getUsername(), deviceId)).map(user -> {
            user.setMode(registerDto.getMode());
            user.setPushServerType(pushServerType);
            user.setLastLoggedInAt(LocalDateTime.now());
            user.setDeviceType(registerDto.getDeviceType());
            user.setDeviceToken(registerDto.getDeviceToken());
            user.setPassword(GatewayUtil.getMd5String(password));
            user.setVoipDeviceToken(registerDto.getVoipDeviceToken());
            user.setDeviceOsVersion(registerDto.getDeviceOsVersion());
            return user;
        }).orElseGet(() -> {
            ChatUserDetails userDetails = new ChatUserDetails();
            userDetails.setDeviceId(deviceId);
            userDetails.setMode(registerDto.getMode());
            userDetails.setPushServerType(pushServerType);
            userDetails.setCreatedAt(LocalDateTime.now());
            userDetails.setUsername(chatUser.getUsername());
            userDetails.setLastLoggedInAt(LocalDateTime.now());
            userDetails.setDeviceType(registerDto.getDeviceType());
            userDetails.setDeviceToken(registerDto.getDeviceToken());
            userDetails.setPassword(GatewayUtil.getMd5String(password));
            userDetails.setDeviceOsVersion(registerDto.getDeviceOsVersion());
            userDetails.setVoipDeviceToken(registerDto.getVoipDeviceToken());
            userDetails.setUserType(applicationProperties.getDefaultUserType());
            return userDetails;
        });

        if (StringUtils.isNotBlank(registerDto.getUserType())) {
            chatUserDetails.setUserType(registerDto.getUserType().trim());
        }
        chatUsersDetailsRepo.save(chatUserDetails);
        return password;
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

	@SwitchDataSource(value = "[0]")
	public void inputDataValidation(String userdomain, SanboxUserRegisterDto registerDto) {
        AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		RegisterDto registerData = new RegisterDto();
		registerData.setDeviceId(registerDto.getDeviceId());
		registerData.setDeviceOsVersion(registerDto.getDeviceOsVersion());
		registerData.setDeviceToken(registerDto.getDeviceToken());
		registerData.setDeviceType(registerDto.getDeviceType());
		registerData.setLicenseKey(registerDto.getLicenseKey());
		registerData.setMobileNumber(registerDto.getMobileNumber());
		registerData.setMode(registerDto.getMode());
		registerData.setUserIdentifier(registerDto.getUserIdentifier());
		registerData.setVoipDeviceToken(registerDto.getVoipDeviceToken());
		registerData.setForceRegister(registerDto.getForceRegister());
		registerData.setPushServerType(registerDto.getPushServerType());
		registerData.setMetaData(registerDto.getMetaData());
		registerData.setUserType(registerDto.getUserType());
		inputDataValidation(userdomain, registerData);
	}

	private void inputDataValidation(String userdomain, RegisterDto registerDto) {
		String userIdentifier = registerDto.getUserIdentifier();
		if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_IDENTIFIER_EMPTY);
		}
		if (userIdentifier.contains("\"") || userIdentifier.contains("&") || userIdentifier.contains("\'")
				|| userIdentifier.contains("/") || userIdentifier.contains(":") || userIdentifier.contains("<")
				|| userIdentifier.contains(">") || userIdentifier.contains("@")) {
			throw new BadDataException("User identifier should not contain (\",&,\',/,:,<,>,@).");
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
		if (registerDto.getPushServerType() != null && !registerDto.getPushServerType().trim().isEmpty()
				&& !registerDto.getPushServerType().equalsIgnoreCase("apns")
				&& !registerDto.getPushServerType().equalsIgnoreCase("fcm")) {
			throw new BadDataException(GatewayConstants.PUSH_SERVER_TYPE_NOT_VALID);
		}
		GatewayUtil.validateMetaData(registerDto.getMetaData(), applicationProperties.getMetaDataLimit());
		GatewayUtil.validateUserType(registerDto.getUserType(), applicationProperties.getUserTypes());

        mobileSession(userdomain, registerDto.getUserIdentifier(), registerDto.getDeviceType());
	}

    private void mobileSession(String userDomain, String userIdentifier, String deviceType){

        int maxSessions = ( int ) dataStore.getConnectionConfigDataByDomainAndKey(userDomain, "userMaxSessions");

        List<ChatUserDetails> existingChatUsersDetails = chatUsersDetailsRepo.findByUsername(userIdentifier);

        log.info("Max Session From DB.. {}, Existing User Session Records Size :: {}", maxSessions, existingChatUsersDetails.size());

        ChatUserDetails earliestSession = getEarliestSession(existingChatUsersDetails, deviceType);

        if ( earliestSession != null ){
            if ( existingChatUsersDetails.size() >= maxSessions ) {

                try {
                    boolean boolStatus = queueService.sendUserSessionLogoutActivity(DataHelperUtils.prepareIQPayload(userDomain, earliestSession.getUsername(), Collections.singleton(earliestSession.getSocketId() != null ? earliestSession.getSocketId() : ""), earliestSession.getDeviceId() != null ? earliestSession.getDeviceId() : ""));
                    log.info("Session Logout IQ Status :: {}", boolStatus);
                } catch (Exception ex){
                    throw new BadDataException("Error While Session Logout IQ :: " + ex.getMessage());
                }

                chatUsersDetailsRepo.delete(earliestSession);

                throw new ForbiddenException(GatewayConstants.USER_REGISTRATION_MAX_SESSIONS_REACHED);
            }
        }

    }

    private ChatUserDetails getEarliestSession(List<ChatUserDetails> chatUserDetails, String deviceType) {

        if (deviceType.equalsIgnoreCase("WEB")) { return null; }

        return chatUserDetails.stream().filter(user -> user.getDeviceType().equalsIgnoreCase("IOS") || user.getDeviceType().equalsIgnoreCase("ANDROID")).min(Comparator.comparing(ChatUserDetails::getLastLoggedInAt)).orElse(null);

    }

    private void handlingExceptionOfApiCall(HttpStatusCodeException e) {
		String responseBody = e.getResponseBodyAsString();
		try {
			Response<?> r = MAPPER.readValue(responseBody, Response.class);
			throw new BadDataException(r.getMessage());
		} catch (JsonProcessingException ex) {
			throw new BadDataException("Error in registration. Please try again later");
		}
	}

}
