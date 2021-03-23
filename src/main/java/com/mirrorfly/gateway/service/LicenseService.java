package com.mirrorfly.gateway.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.LicenseAVResDto;
import com.mirrorfly.gateway.dto.LicenseChatResDto;
import com.mirrorfly.gateway.dto.LicenseDataDto;
import com.mirrorfly.gateway.dto.LicenseReqDto;
import com.mirrorfly.gateway.dto.LicenseResDto;
import com.mirrorfly.gateway.dto.LicenseUpdateRequestDto;
import com.mirrorfly.gateway.dto.TurnDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.licenseutil.AESEncryption;
import com.mirrorfly.gateway.model.ChatServerConfig;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.License;
import com.mirrorfly.gateway.model.LicenseLookup;
import com.mirrorfly.gateway.repo.ChatServerConfigRepo;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
import com.mirrorfly.gateway.repo.LicenseLookupRepo;
import com.mirrorfly.gateway.repo.LicenseRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.EnumSDKType;
import com.mirrorfly.gateway.util.GatewayConstants;

@Service
public class LicenseService {

	@Autowired
	private LicenseRepo licenseRepo;

	@Autowired
	private ChatSettingsRepo chatSettingsRepo;

	@Autowired
	private ChatServerConfigRepo chatServerConfigRepo;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private LicenseLookupRepo licenseLookupRepo;

	public Response<LicenseResDto> getLicense(LicenseReqDto licenseReq) {

		Response<LicenseResDto> response = new Response<>();
		String SDKType = licenseReq.getSDK();
		ChatSettings settings = getConfigService();

		License license = licenseRepo.findByActivationKeyAndSdk(licenseReq.getActivationKey(), SDKType);
		if (license == null) {
			throw new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL);
		}
		if (license.getIsActive().equals(1)) {
			throw new BadDataException(GatewayConstants.LICENSE_IN_ACTIVE);
		}

		LocalDate curentDate = LocalDate.now();
		int days = license.getExpireDate().compareTo(curentDate);

		if (days < 0) {
			throw new BadDataException(GatewayConstants.LICENSE_EXPIRED);
		}

		if (EnumSDKType.iOSChat.getSdkType().equals(SDKType) || EnumSDKType.AndroidChat.getSdkType().equals(SDKType)
				|| EnumSDKType.WebChat.getSdkType().equals(SDKType)) {
			LicenseChatResDto info = new LicenseChatResDto();
			info.setXmppDomain(settings.getXmppDomain());
			info.setXmppPort(settings.getXmppPort());
			info.setBaseUrl(applicationProperties.getApiBaseUrl());
			response.setData(info);
		} else if (EnumSDKType.iOSAVCalls.getSdkType().equals(SDKType)
				|| EnumSDKType.AndroidAVCalls.getSdkType().equals(SDKType)
				|| EnumSDKType.WebAVCalls.getSdkType().equals(SDKType)) {
			LicenseAVResDto info = new LicenseAVResDto();
			info.setSignalServer(settings.getSignalServerDomain());
			getStunAndTurnServers(settings, info);
			response.setData(info);
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	private void getStunAndTurnServers(ChatSettings settings, LicenseAVResDto info) {
		List<ChatServerConfig> chatServerConfig = chatServerConfigRepo.findByChatConfigId(settings.getId());
		if (chatServerConfig != null && !chatServerConfig.isEmpty()) {
			List<String> stuns = new ArrayList<>();
			List<TurnDto> dtoList = new ArrayList<>();
			for (ChatServerConfig chatServer : chatServerConfig) {
				if (1 == chatServer.getIsTurn()) {
					dtoList.add(new TurnDto(chatServer.getUrl(), chatServer.getUsername(), chatServer.getPassword()));
				}
				if (0 == chatServer.getIsTurn()) {
					stuns.add(chatServer.getUrl());
				}
			}
			info.setStuns(stuns);
			info.setTurns(dtoList);
		}
	}

	public ChatSettings getConfigService() {
		return chatSettingsRepo.findAll().get(1);
	}

	public Response<String> updateLicense(LicenseUpdateRequestDto request) {
		List<LicenseLookup> existingKey = licenseLookupRepo.findAll();
		if (!existingKey.isEmpty() && existingKey.get(0) != null) {

			String encryptKey = GatewayConstants.ENCRYPTKEY + GatewayConstants.ENCRYPTKEY + GatewayConstants.ENCRYPTKEY;
			String decyptedString = AESEncryption.decryptString(request.getLicenseKey(), encryptKey);
			ObjectMapper mapper = new ObjectMapper();
			try {
				mapper.readValue(URLDecoder.decode(decyptedString), LicenseDataDto.class);
			} catch (JsonProcessingException e) {
				throw new BadDataException("Invalid license key");
			}
			LicenseLookup data = existingKey.get(0);
			data.setLkey(request.getLicenseKey());
			licenseLookupRepo.save(data);
		} else {
			throw new BadDataException("License key not found");
		}
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.SC_OK);
		response.setMessage("License key updated successfully");
		response.setData("true");
		return response;
	}

}
