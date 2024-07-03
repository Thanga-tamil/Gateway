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

	public ConfigDto getConfigData(String licenseKey) {
		ChatSettings settings = getChatSettingsData(licenseKey);
		ConfigDto config = new ConfigDto();
		config.setDomain(settings.getDomain());
		config.setAudioLimit(settings.getAudioLimit());
		config.setDomain(settings.getDomain());
		config.setPrivateTime(settings.getPrivateTime());
		config.setRecallTime(settings.getRecallTime());
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
		config.setGroupChat(settings.getGroupChat() == 1);
		config.setAttachment(settings.getAttachment() == 1);
		config.setImageAttachment(settings.getImageAttachment() == 1);
		config.setVideoAttachment(settings.getVideoAttachment() == 1);
		config.setAudioAttachment(settings.getAudioAttachment() == 1);
		config.setDocumentAttachment(settings.getDocumentAttachment() == 1);
		config.setContactAttachment(settings.getContactAttachment() == 1);
		config.setLocationAttachment(settings.getLocationAttachment() == 1);
		config.setOne2oneCall(settings.getOne2oneCall() == 1);
		config.setGroupCall(settings.getGroupCall() == 1);
		config.setRecentchatSearch(settings.getRecentchatSearch() == 1);
		config.setStarMessage(settings.getStarMessage() == 1);
		config.setClearChat(settings.getClearChat() == 1);
		config.setDeleteChat(settings.getDeleteChat() == 1);
		config.setTranslation(settings.getTranslation() == 1);
		config.setDeleteMessage(settings.getDeleteMessage() == 1);
		config.setReport(settings.getReport() == 1);
		config.setBlock(settings.getBlock() == 1);
		config.setViewAllMedias(settings.getViewAllMedias() == 1);
		config.setChatHistory(settings.getChatHistory() == 1);
		config.setCallReconnectionTimeOut(settings.getCallReconnectionTimeOut());
		config.setChatSummaryAiInterval(settings.getChatSummaryAiInterval());
		getStunAndTurnServers(config);
		return config;
	}

	private void getStunAndTurnServers(ConfigDto config) {
		List<ChatServerConfig> chatServerConfig = chatServerConfigRepo.findAll();
		if (!chatServerConfig.isEmpty()) {
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

	public ChatSettings getChatSettingsData(String licenseKey) {
		ChatSettings settings = null;
		if (licenseKey != null && !licenseKey.isEmpty()) {
			settings = chatSettingsRepo.findByLicenseKey(licenseKey);
		}
		if (settings == null) {
			settings = chatSettingsRepo.findAll().get(0);
		}
		return settings;
	}

}
