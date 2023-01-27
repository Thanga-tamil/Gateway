package com.mirrorfly.gateway.service;

import java.time.LocalDateTime;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.ChatSettingDataDto;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
import com.mirrorfly.gateway.response.Response;

@Service
public class ChatSettingService {

	@Autowired
	private ChatSettingsRepo chatSettingsRepo;

	public Response<String> updateChatSetting(ChatSettingDataDto request) {
		ChatSettings chatSettingValue = chatSettingsRepo.findByLicenseKey(request.getLicenceKey());
		if (chatSettingValue != null) {
			updateChatSettings(request, chatSettingValue);
		} else {
			ChatSettings ChatSettingsValue = chatSettingsRepo.findBySingleChatSettingData();
			createChatSettings(request, ChatSettingsValue);
		}
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.SC_OK);
		response.setMessage("Chat setting updated successfully");
		response.setData("true");
		return response;
	}

	private void createChatSettings(ChatSettingDataDto request, ChatSettings ChatSettingsValue) {
		LocalDateTime currentTimestamp = LocalDateTime.now();
		ChatSettings chatSettings = new ChatSettings();
		chatSettings.setDomain(ChatSettingsValue.getDomain());
		chatSettings.setAdminUser(ChatSettingsValue.getAdminUser());
		chatSettings.setMailLogin(ChatSettingsValue.getMailLogin());
		chatSettings.setVideoLimit(ChatSettingsValue.getVideoLimit());
		chatSettings.setAudioLimit(ChatSettingsValue.getAudioLimit());
		chatSettings.setRecallTime(ChatSettingsValue.getRecallTime());
		chatSettings.setPrivateTime(ChatSettingsValue.getPrivateTime());
		chatSettings.setStatus(ChatSettingsValue.getStatus().toString());
		chatSettings.setFeedbackFrom(ChatSettingsValue.getFeedbackFrom());
		chatSettings.setFeedbackTo(ChatSettingsValue.getFeedbackTo());
		chatSettings.setXmppDomain(ChatSettingsValue.getXmppDomain());
		chatSettings.setSignalServerDomain(ChatSettingsValue.getSignalServerDomain());
		chatSettings.setXmppHost(ChatSettingsValue.getXmppHost());
		chatSettings.setGoogleTranslate(ChatSettingsValue.getGoogleTranslate());
		chatSettings.setNotificationHelpUrl(ChatSettingsValue.getNotificationHelpUrl());
		chatSettings.setXmppPort(ChatSettingsValue.getXmppPort());
		chatSettings.setSdkUrl(ChatSettingsValue.getSdkUrl());
		chatSettings.setPinExpireDays(ChatSettingsValue.getPinExpireDays());
		chatSettings.setPinTimeOut(ChatSettingsValue.getPinTimeOut());
		chatSettings.setFileSizeLimit(ChatSettingsValue.getFileSizeLimit());
		chatSettings.setIsLiveStreamingEnabled(ChatSettingsValue.getIsLiveStreamingEnabled());
		chatSettings.setLiveStreamingSignalServer(ChatSettingsValue.getLiveStreamingSignalServer());
		chatSettings.setSipcallEnabled(ChatSettingsValue.getSipcallEnabled());
		chatSettings.setSipServer(ChatSettingsValue.getSipServer());
		chatSettings.setChatBackupType(ChatSettingsValue.getChatBackupType());
		chatSettings.setChatBackupFrequency(ChatSettingsValue.getChatBackupFrequency());
		chatSettings.setCallRoutingServer(ChatSettingsValue.getCallRoutingServer());
		chatSettings.setXmppPortWeb(ChatSettingsValue.getXmppPortWeb());
		chatSettings.setIv(ChatSettingsValue.getIv());
		chatSettings.setIvProfile(ChatSettingsValue.getIvProfile());

		chatSettings.setGroupChat(request.getGroupChat());
		chatSettings.setAttachment(request.getAttachment());
		chatSettings.setImageAttachment(request.getImageAttachment());
		chatSettings.setVideoAttachment(request.getVideoAttachment());
		chatSettings.setAudioAttachment(request.getAudioAttachment());
		chatSettings.setDocumentAttachment(request.getDocumentAttachment());
		chatSettings.setContactAttachment(request.getContactAttachment());
		chatSettings.setLocationAttachment(request.getLocationAttachment());
		chatSettings.setOne2oneCall(request.getOne2oneCall());
		chatSettings.setGroupCall(request.getGroupCall());
		chatSettings.setRecentchatSearch(request.getRecentchatSearch());
		chatSettings.setLicenseKey(request.getLicenceKey());
		chatSettings.setCreatedAt(currentTimestamp);
		chatSettings.setUpdatedAt(currentTimestamp);
		chatSettingsRepo.save(chatSettings);
	}

	private void updateChatSettings(ChatSettingDataDto request, ChatSettings chatSettings) {
		LocalDateTime currentTimestamp = LocalDateTime.now();
		chatSettings.setGroupChat(request.getGroupChat());
		chatSettings.setAttachment(request.getAttachment());
		chatSettings.setImageAttachment(request.getImageAttachment());
		chatSettings.setVideoAttachment(request.getVideoAttachment());
		chatSettings.setAudioAttachment(request.getAudioAttachment());
		chatSettings.setDocumentAttachment(request.getDocumentAttachment());
		chatSettings.setContactAttachment(request.getContactAttachment());
		chatSettings.setLocationAttachment(request.getLocationAttachment());
		chatSettings.setOne2oneCall(request.getOne2oneCall());
		chatSettings.setGroupCall(request.getGroupCall());
		chatSettings.setRecentchatSearch(request.getRecentchatSearch());
		chatSettings.setUpdatedAt(currentTimestamp);
		chatSettingsRepo.save(chatSettings);
	}

}
