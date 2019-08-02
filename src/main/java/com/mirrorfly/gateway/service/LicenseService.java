package com.mirrorfly.gateway.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.LicenseReqDto;
import com.mirrorfly.gateway.dto.LicenseResDto;
import com.mirrorfly.gateway.dto.TurnDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatServerConfig;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.License;
import com.mirrorfly.gateway.repo.ChatServerConfigRepo;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
import com.mirrorfly.gateway.repo.LicenseRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;

@Service
public class LicenseService {
	
	@Autowired
	private LicenseRepo licenseRepo;
	
	@Autowired
	private ChatSettingsRepo chatSettingsRepo;
	
	@Autowired
	private ChatServerConfigRepo chatServerConfigRepo;

	public Response<LicenseResDto> getLicense(LicenseReqDto licenseReq) {
		License license = licenseRepo.findByActivationKey(licenseReq.getActivationKey());
		if (license == null) {
			throw new BadDataException(GatewayConstants.LICENSE_NOT_AVAIL);
		}
		if (license.getIsActive().equals(1)) {
			throw new BadDataException(GatewayConstants.LICENSE_IN_ACTIVE);
		}
		LocalDate curentDate = LocalDate.now();
		int days = license.getExpireDate().compareTo(curentDate);
		if(days < 0) {
			throw new BadDataException(GatewayConstants.LICENSE_EXPIRED);
		}
		
		ChatSettings settings = getConfigService();
		LicenseResDto info = new LicenseResDto(); 
		info.setSignalServer(settings.getSignalServerDomain());
		getStunAndTurnServers(settings, info);
		Response<LicenseResDto> response = new Response<>();
		response.setData(info);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}
	
	private void getStunAndTurnServers(ChatSettings settings, LicenseResDto info) {
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
		return chatSettingsRepo.findAll().get(0);
	}

}
