package com.mirrorfly.gateway.service;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
import com.mirrorfly.gateway.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

	private final ApplicationProperties applicationProperties;
	private final TokenService tokenService;
	private final PasswordService passwordService;
	private final ConfigService configService;
	private final VcardService vcardService;
	private final RestTemplate restTemplate;
	private final DataStore dataStore;
	private final QueueService queueService;
	private final UserRepo userRepo;
	private final ChatUserRepo chatUserRepo;
	private final ChatUsersDetailsRepo chatUsersDetailsRepo;
	private final SpoolRepo spoolRepo;
	private final LastRepo lastRepo;
	private final ChatSettingsRepo settingsRepo;
	private final UserContactRepo userContactRepo;
	private final RosterUserRepo rosterUserRepo;
	private final PrivacySettingsRepo privacySettingsRepo;
	private final PrivacyUsersListRepo privacyUsersListRepo;
	private final TokenRepo tokenRepo;
	private final StringRedisTemplate stringRedisTemplate;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/** ---------- Register Info -------------- */

	@SwitchDataSource("[0]")
	public Response<RegisterInfoDto> getRegisterInfo(String userdomain) {
		
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		ChatSettings settings = settingsRepo.findAll().get(0);

		RegisterInfoDto info = new RegisterInfoDto();
		info.setDomain(settings.getDomain());
		info.setMailLogin(settings.getMailLogin());

		return new Response<RegisterInfoDto>(HttpStatus.OK.value(), info, GatewayConstants.DATA_RETRIEVED);
	}

	/** ---------- User Registration -------------- */

	@SwitchDataSource("[0]")
	public Response<String> registerNewUser(String userdomain, RegisterRequestDto req) {

		String licenseKey = Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "licenseKey")).orElseThrow(() -> new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL));

		String xmppDomain = Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "xmppDomain")).orElseThrow(() -> new BadDataException("Xmpp Domain not found"));

		RegisterDto registerDto = decryptAndParse(req.getRegisterData(), licenseKey);
		registerDto.setMobileNumber(registerDto.getCountryCode() + registerDto.getMobileNumber());

		RegisterResponseDto responseDto = registerUser(userdomain, registerDto);
		responseDto.setConfig(configService.getConfigData(licenseKey));

		asyncPostRegister(userdomain, responseDto.getUsername(), registerDto.getMobileNumber(), xmppDomain);

		String encryptData = AESEncryption.encryptString(toJson(responseDto), licenseKey.repeat(3));
		
		return new Response<String>(HttpStatus.OK.value(), encryptData, GatewayConstants.DATA_RETRIEVED);
	}
	
	/** ---------- Core Registration -------------- */

	private RegisterResponseDto registerUser(String userdomain, RegisterDto dto) {

		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		inputDataValidation(dto);

		ChatUser chatUser = chatUserRepo.findByUserIdentifier(dto.getUserIdentifier());
		Users user;
		RegisterResponseDto response = new RegisterResponseDto();

		if (chatUser == null) {
			validateLicense(dto.getLicenseKey());
			chatUser = createNewChatUser(dto);
			user = new Users();
			user.setUsername(dto.getUserIdentifier());
			registerResponseDefaults(response, false, false);
		} else {
			user = Optional.ofNullable(userRepo.findByUsername(chatUser.getUsername())).orElseGet(() -> {
				Users u = new Users();
				u.setUsername(dto.getUserIdentifier());
				return u;
			});
			validateExistingUser(chatUser);
			registerResponseDefaults(response, true, true);
		}

		chatUser.setMetaData(GatewayUtil.convertMetaDataAsJson(dto.getMetaData()));
		chatUserRepo.save(chatUser);

		String deviceId = resolveDeviceId(dto);
		boolean shouldClearPrivacyUsersList = shouldClearPrivacyUsersList(dto, chatUser.getUsername(), deviceId);
		log.info("[privacy_users_list] register check | userdomain={} username={} deviceType={} deviceId={} shouldClear={}",
				userdomain, chatUser.getUsername(), dto.getDeviceType(), deviceId, shouldClearPrivacyUsersList);

		String password = updateChatUserDetails(userdomain, dto, chatUser);
		response.setPassword(password);

		updateUserPassword(user, password, dto.getDeviceType());
		response.setUsername(chatUser.getUsername());
		response.setToken(tokenService.asyncGetToken(userdomain, chatUser.getUsername(), deviceId, ""));

		userRepo.save(user);

		if (shouldClearPrivacyUsersList) {
			log.info("[privacy_users_list] scheduling async delete | userdomain={} username={}", userdomain, chatUser.getUsername());
			asyncClearPrivacyUsersList(userdomain, chatUser.getUsername());
		} else {
			log.info("[privacy_users_list] skip delete | userdomain={} username={}", userdomain, chatUser.getUsername());
		}

		return response;
	}

	/** ---------- Update Chat Users Details -------------- */

	public String updateChatUserDetails(String userdomain, RegisterDto dto, ChatUser chatUser) {

		String deviceId = resolveDeviceId(dto);
		String pushServerType = resolvePushServerType(dto);
		clearOldTokens(dto);

		ChatUserDetails details = chatUsersDetailsRepo.findByUsernameAndDeviceId(chatUser.getUsername(), deviceId);

		String password = passwordService.generateRandomPassword();

		if (details == null) {
			removeOldSessionIfNeeded(chatUser.getUsername(), dto.getDeviceType(), userdomain);
			details = new ChatUserDetails();
			details.setUsername(chatUser.getUsername());
			details.setDeviceId(deviceId);
			details.setCreatedAt(LocalDateTime.now());
		}

		populateUserDetails(details, dto, password, pushServerType);
		chatUsersDetailsRepo.save(details);
		return password;
	}

	/** ---------- Helpers Method -------------- */

	private void validateLicense(String licenseKey) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(applicationProperties.getWebportalApiBaseUrl() + "/license/validate").queryParam("licenseKey", licenseKey);
			restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
		} catch (HttpStatusCodeException e) {
			handlingExceptionOfApiCall(e);
		}
	}

	private ChatUser createNewChatUser(RegisterDto dto) {
		ChatUser user = new ChatUser();
		user.setUsername(dto.getUserIdentifier());
		user.setMobileNumber(dto.getMobileNumber());
		user.setUserIdentifier(dto.getUserIdentifier());
		user.setLicenseKey(dto.getLicenseKey());
		user.setDevicetype(dto.getDeviceType());
		user.setCreatedAt(GatewayUtil.getCurrentDateTime());
		user.setIsDeleteInprogrees(0);

		registerLogintime(dto.getUserIdentifier());

		VcardSearch vcardSearch = vcardService.updateVcardSearchDetails(dto.getUserIdentifier());
		vcardService.updateVcard(vcardSearch, new Vcard());

		return user;
	}

	private void validateExistingUser(ChatUser chatUser) {
		if (chatUser.getIsDeleteInprogrees() == 1)
			throw new BadDataException(GatewayConstants.DELETE_INPROGRESS);

		VcardSearch vcardSearch = vcardService.findVcardSearchByUserName(chatUser.getUsername());

		if (vcardSearch != null && vcardSearch.getIsBlock() == 1)
			throw new ForbiddenException(GatewayConstants.BLOCK_USER);
	}

	private void registerResponseDefaults(RegisterResponseDto r, boolean profile, boolean existing) {
		r.setIsProfileUpdated(profile);
		r.setIsExisting(existing);
	}

	private void updateUserPassword(Users user, String password, String deviceType) {
		if (GatewayConstants.WEB.equalsIgnoreCase(deviceType))
			user.setWsPassword(GatewayUtil.getMd5String(password));
		else
			user.setPassword(GatewayUtil.getMd5String(password));
	}

	private String resolveDeviceId(RegisterDto dto) {
		String suffix = dto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB) ? "-web" : "-mobile";
		return StringUtils.defaultIfBlank(dto.getDeviceId(), "MF-01" + suffix);
	}

	private boolean shouldClearPrivacyUsersList(RegisterDto dto, String username, String incomingDeviceId) {
		if (GatewayConstants.DEVICE_WEB.equalsIgnoreCase(dto.getDeviceType())) {
			log.info("[privacy_users_list] web session — no clear needed | username={} deviceId={}", username, incomingDeviceId);
			return false;
		}

		// Only clear when DB already has mobile sessions for this user,
		// and none of the stored device_ids match the incoming device_id.
		List<ChatUserDetails> existingSessions = chatUsersDetailsRepo.findByUsername(username);
		if (existingSessions == null || existingSessions.isEmpty()) {
			log.info("[privacy_users_list] no existing chat_users_details rows — skip clear | username={} incomingDeviceId={}",
					username, incomingDeviceId);
			return false;
		}

		boolean hasIncomingDevice = existingSessions.stream()
				.anyMatch(s -> incomingDeviceId.equals(s.getDeviceId()));
		log.info("[privacy_users_list] mobile session check | username={} incomingDeviceId={} hasIncomingDevice={}",
				username, incomingDeviceId, hasIncomingDevice);

		return !hasIncomingDevice;
	}

	@Async("contactsyncExecutor")
	@SwitchDataSource("[0]")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CompletableFuture<Void> asyncClearPrivacyUsersList(String userdomain, String username) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("[privacy_users_list] async delete started | userdomain={} username={}", userdomain, username);
				AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
				int existingCount = privacyUsersListRepo.findByUsername(username).size();
				log.info("[privacy_users_list] records found before delete | userdomain={} username={} count={}",
						userdomain, username, existingCount);
				privacyUsersListRepo.deleteByUsername(username);
				log.info("[privacy_users_list] async delete completed | userdomain={} username={} deletedCount={}",
						userdomain, username, existingCount);

				int updatedRows = chatUserRepo.resetPrivacyTypeByUsername(username);
				if (updatedRows > 0) {
					log.info("[privacy_users_list] reset chat_users privacy_type=0 | userdomain={} username={} updatedRows={}",
							userdomain, username, updatedRows);
				} else {
					log.info("[privacy_users_list] no chat_users entry — skip privacy_type reset | userdomain={} username={}",
							userdomain, username);
				}
			} catch (Exception e) {
				log.error("[privacy_users_list] async delete failed | userdomain={} username={} error={}",
						userdomain, username, e.getMessage(), e);
			}
		});
	}

	private String resolvePushServerType(RegisterDto dto) {
		if (GatewayConstants.DEVICE_ANDROID.equalsIgnoreCase(dto.getDeviceType()))
			return "fcm";
		return StringUtils.defaultIfBlank(dto.getPushServerType(), "apns");
	}

	private void clearOldTokens(RegisterDto dto) {
		if (StringUtils.isNotBlank(dto.getDeviceToken()))
			chatUsersDetailsRepo.updateDeviceTokenAsEmpty(dto.getDeviceToken());
		if (StringUtils.isNotBlank(dto.getVoipDeviceToken()))
			chatUsersDetailsRepo.updateVoipDeviceTokenAsEmpty(dto.getVoipDeviceToken());
	}

	private void populateUserDetails(ChatUserDetails u, RegisterDto d, String password, String pushServerType) {

		u.setDeviceOsVersion(StringUtils.defaultIfBlank(d.getDeviceOsVersion(), "12.0"));
		u.setDeviceToken(d.getDeviceToken());
		u.setVoipDeviceToken(d.getVoipDeviceToken());
		u.setDeviceType(d.getDeviceType());
		u.setLastLoggedInAt(LocalDateTime.now());
		u.setMode(d.getMode());
		u.setPassword(GatewayUtil.getMd5String(password));
		u.setPushServerType(pushServerType);
		u.setUserType(StringUtils.defaultIfBlank(d.getUserType(), applicationProperties.getDefaultUserType()));
	}

	private void removeOldSessionIfNeeded(String username, String deviceType, String domain) {

		List<ChatUserDetails> sessions = chatUsersDetailsRepo.findByUsername(username);

		if (sessions == null || sessions.isEmpty())
			return;

		ChatUserDetails deletable = findDeletableChatUsersDetails(sessions, deviceType);

		if (deletable == null)
			return;

		chatUsersDetailsRepo.delete(deletable);
		spoolRepo.deleteOfflilneRecordsByUsernameAndDeviceId(deletable.getUsername(), deletable.getDeviceId());

		try {
			List<TokenDetail> tokens = tokenRepo.findByUsernameAndDevicetype(username, deletable.getDeviceId());

			if (!tokens.isEmpty()) {
				try {
					List<String> redisKeys = tokens.stream()
							.map(TokenDetail::getToken)
							.toList();

					stringRedisTemplate.delete(redisKeys);
					tokenRepo.deleteAll(tokens);

				} catch (Exception e) {
					log.error("[Register] Error while removing tokens from Redis", e);
				}
			}
			queueService.sendUserSessionLogoutActivity(DataHelperUtils.prepareIQPayload(domain, deletable.getUsername(), deletable.getDeviceId(), Collections.singleton(deletable.getSocketId()), Boolean.FALSE));
		} catch (JsonProcessingException e) {
            log.error("IQ logout failed :: {}", e.getMessage());
		}
	}

	private ChatUserDetails findDeletableChatUsersDetails(List<ChatUserDetails> list, String deviceType) {

		boolean incomingWeb = GatewayConstants.DEVICE_WEB.equalsIgnoreCase(deviceType);

		return list.stream().filter(s -> incomingWeb == GatewayConstants.DEVICE_WEB.equalsIgnoreCase(s.getDeviceType())).min(Comparator.comparing(ChatUserDetails::getLastLoggedInAt)).orElse(null);
	}

	/** ---------- Validation Method -------------- */

	private RegisterDto decryptAndParse(String data, String key) {
		try {
			String decrypted = AESEncryption.decryptString(data, key + key + key);
			return MAPPER.readValue(URLDecoder.decode(decrypted, StandardCharsets.UTF_8), RegisterDto.class);
		} catch (Exception e) {
			throw new BadDataException(GatewayConstants.INVALID_ERROR);
		}
	}

	private String toJson(Object o) {
		try {
			return MAPPER.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}
	}

	public void registerLogintime(String username) {
		Last last = new Last();
		last.setUsername(username);
		last.setStatus(0);
		last.setPresence(0);
		last.setState("Stream reset by peer");
		last.setSeconds(BigInteger.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
		lastRepo.save(last);
	}

	private void handlingExceptionOfApiCall(HttpStatusCodeException e) {
		try {
			Response<?> r = MAPPER.readValue(e.getResponseBodyAsString(), Response.class);
			throw new BadDataException(r.getMessage());
		} catch (Exception ex) {
			throw new BadDataException("Error in registration. Please try again later");
		}
	}
	
	private void inputDataValidation(RegisterDto registerDto) {

		String userIdentifier = registerDto.getUserIdentifier();

		if (StringUtils.isBlank(userIdentifier)) {
			throw new BadDataException(GatewayConstants.USER_IDENTIFIER_EMPTY);
		}

		if (userIdentifier.contains("\"") || userIdentifier.contains("&") || userIdentifier.contains("'") || userIdentifier.contains("/") || userIdentifier.contains(":") || userIdentifier.contains("<") || userIdentifier.contains(">") || userIdentifier.contains("@")) {
			throw new BadDataException("User identifier should not contain (\",&,' ,/,:,<,>,@).");
		}

		if (StringUtils.isBlank(registerDto.getLicenseKey())) {
			throw new BadDataException(GatewayConstants.USER_LICENSEKEY_EMPTY);
		}

		if (StringUtils.isBlank(registerDto.getDeviceType())) {
			throw new BadDataException(GatewayConstants.DEVICE_TYPE_EMPTY);
		}

		if (!registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_ANDROID) && !registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_IOS) && !registerDto.getDeviceType().equalsIgnoreCase(GatewayConstants.DEVICE_WEB)) {
			throw new BadDataException(GatewayConstants.DEVICE_TYPE_NOT_VALID);
		}

		if (StringUtils.isNotBlank(registerDto.getPushServerType()) && !registerDto.getPushServerType().equalsIgnoreCase("apns") && !registerDto.getPushServerType().equalsIgnoreCase("fcm")) {
			throw new BadDataException(GatewayConstants.PUSH_SERVER_TYPE_NOT_VALID);
		}

		GatewayUtil.validateMetaData(registerDto.getMetaData(), applicationProperties.getMetaDataLimit());

		GatewayUtil.validateUserType(registerDto.getUserType(), applicationProperties.getUserTypes());
	}

	@Async("contactsyncExecutor")
	@SwitchDataSource("[0]")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public CompletableFuture<Void> asyncPostRegister(String userdomain, String username, String contactNo, String xmppDomain) {

		return CompletableFuture.runAsync(() -> {

			AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

			/** -------------------- Update RosterUsers -------------------- */
			
			if (StringUtils.isNotBlank(contactNo)) {

				// Reverse the contact number for efficient index lookup on mobile_number_reversed_trim
				String reversedContactNo = new StringBuilder(contactNo.trim()).reverse().toString();

				List<UserContact> contacts = userContactRepo.findByMobileNumber(reversedContactNo);

				if (contacts != null && !contacts.isEmpty()) {

					Set<String> usernames = contacts.stream().map(UserContact::getUsername).collect(Collectors.toSet());
					String jid = username + "@" + xmppDomain;
					List<String> existingRosters = rosterUserRepo.findAllRosters(usernames, jid);
					List<RosterUser> newRosters = contacts.stream().filter(c -> !existingRosters.contains(c.getUsername())).map(c -> new RosterUser(new RosterUserKey(c.getUsername(), jid), 'N', 'N', username, c.getName(), 'B', "item", c.getLicenseKey())).toList();

					if (!newRosters.isEmpty()) {
						rosterUserRepo.saveAll(newRosters);
					}
					userContactRepo.deleteAll(contacts);
				}
			}

			/** -------------------- Update Privacy Settings -------------------- */
			
			PrivacySettingsEntity privacySetting = privacySettingsRepo.findByUsername(username);
			if (privacySetting == null) {
				PrivacySettingsEntity privacySettingsEntity = new PrivacySettingsEntity();
				privacySettingsEntity.setUsername(username);
				privacySettingsEntity.setSeenReceipt(1);
				privacySettingsEntity.setHideProfile(1);
				privacySettingsEntity.setOnlineStatus(1);
				privacySettingsEntity.setCreatedAt(LocalDateTime.now());
				privacySettingsRepo.save(privacySettingsEntity);
			}
			
		});
	}

}
