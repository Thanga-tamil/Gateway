package com.mirrorfly.gateway.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.LicenseAVResDto;
import com.mirrorfly.gateway.dto.LicenseChatResDto;
import com.mirrorfly.gateway.dto.LicenseReqDto;
import com.mirrorfly.gateway.dto.LicenseResDto;
import com.mirrorfly.gateway.dto.LicenseUpdateRequestDto;
import com.mirrorfly.gateway.dto.TurnDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatServerConfig;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.License;
import com.mirrorfly.gateway.model.LicenseLookup;
import com.mirrorfly.gateway.queue.LicenseDataPayload;
import com.mirrorfly.gateway.queue.QueueService;
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
	
	@Autowired
	private QueueService queueService;

	public Response<LicenseResDto> getLicense(LicenseReqDto licenseReq) {

		Response<LicenseResDto> response = new Response<>();
		String sdkType = licenseReq.getSdk();
		ChatSettings settings = getConfigService();

		License license = licenseRepo.findByActivationKeyAndSdk(licenseReq.getActivationKey(), sdkType);
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

		if (EnumSDKType.IOSCHAT.getSdkType().equals(sdkType) || EnumSDKType.ANDROIDCHAT.getSdkType().equals(sdkType)
				|| EnumSDKType.WEBCHAT.getSdkType().equals(sdkType)) {
			LicenseChatResDto info = new LicenseChatResDto();
			info.setXmppDomain(settings.getXmppDomain());
			info.setXmppPort(settings.getXmppPort());
			info.setBaseUrl(applicationProperties.getApiBaseUrl());
			response.setData(info);
		} else if (EnumSDKType.IOSAVCALLS.getSdkType().equals(sdkType)
				|| EnumSDKType.ANDROIDAVCALLS.getSdkType().equals(sdkType)
				|| EnumSDKType.WEBAVCALLS.getSdkType().equals(sdkType)) {
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
		LicenseLookup data = new LicenseLookup();
		if (!existingKey.isEmpty() && existingKey.get(0) != null) {
			data = existingKey.get(0);
		}
		data.setLkey(request.getLicenseKey());
		licenseLookupRepo.save(data);
		queueService.sendLicenseDataPayload(new LicenseDataPayload(request.getLicenseKey()));
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.SC_OK);
		response.setMessage("License key updated successfully");
		response.setData("true");
		return response;
	}

}
