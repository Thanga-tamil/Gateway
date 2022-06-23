package com.mirrorfly.gateway.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.ConfigDto;
import com.mirrorfly.gateway.dto.TurnDto;
import com.mirrorfly.gateway.model.ChatServerConfig;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.repo.ChatServerConfigRepo;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;

@Service
public class ConfigService {

	@Autowired
	private ChatSettingsRepo chatSettingsRepo;

	@Autowired
	private ChatServerConfigRepo chatServerConfigRepo;

	public ConfigDto getCongigData() {
		ChatSettings settings = getConfigService();
		ConfigDto config = new ConfigDto();
		config.setDomain(settings.getDomain());
		config.setAudioLimit(settings.getAudioLimit());
		config.setDomain(settings.getDomain());
		config.setPrivateTime(settings.getPrivateTime());
		config.setRecallTime(settings.getRecallTime());
		getStunAndTurnServers(settings, config);
		config.setVideoLimit(settings.getVideoLimit());
		config.setSignalServerDomain(settings.getSignalServerDomain());
		config.setXmppDomain(settings.getXmppDomain());
		config.setXmppHost(settings.getXmppHost());
		config.setXmppPort(settings.getXmppPort());
		config.setGoogleTranslate(settings.getGoogleTranslate());
		config.setNotificationHelpUrl(settings.getNotificationHelpUrl());
		config.setSdkUrl(settings.getSdkUrl());
		config.setAdminUser(settings.getAdminUser());
		config.setPinExpireDays(settings.getPinExpireDays());
		config.setPinTimeOut(settings.getPinTimeOut());
		config.setFileSizeLimit(settings.getFileSizeLimit());
		config.setLiveStreamingSignalServer(settings.getLiveStreamingSignalServer());
		config.setIsLiveStreamingEnabled(settings.getIsLiveStreamingEnabled() == 1);
		config.setSipcallEnabled(settings.getSipcallEnabled());
		config.setSipServer(settings.getSipServer());
		config.setCallRoutingServer(settings.getCallRoutingServer());
		config.setChatBackupType(settings.getChatBackupType());
		config.setChatBackupFrequency(settings.getChatBackupFrequency());
		config.setXmppPortWeb(settings.getXmppPortWeb());
		config.setIv(settings.getIv());
		config.setIvProfile(settings.getIvProfile());
		return config;
	}

	private void getStunAndTurnServers(ChatSettings settings, ConfigDto config) {
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
			config.setStuns(stuns);
			config.setTurns(dtoList);
		}
	}

	public ChatSettings getConfigService() {
		return chatSettingsRepo.findAll().get(0);
	}

}
