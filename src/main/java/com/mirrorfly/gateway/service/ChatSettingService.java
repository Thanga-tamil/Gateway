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
			ChatSettings singleChatSettingsValue = chatSettingsRepo.findBySingleChatSettingData();
			createChatSettings(request, singleChatSettingsValue);
		}
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.SC_OK);
		response.setMessage("Chat setting updated successfully");
		response.setData("true");
		return response;
	}

	private void createChatSettings(ChatSettingDataDto request, ChatSettings singleChatSettingsValue) {
		LocalDateTime currentTimestamp = LocalDateTime.now();
		ChatSettings chatSettings = new ChatSettings();
		chatSettings.setDomain(singleChatSettingsValue.getDomain());
		chatSettings.setAdminUser(singleChatSettingsValue.getAdminUser());
		chatSettings.setMailLogin(singleChatSettingsValue.getMailLogin());
		chatSettings.setVideoLimit(singleChatSettingsValue.getVideoLimit());
		chatSettings.setAudioLimit(singleChatSettingsValue.getAudioLimit());
		chatSettings.setRecallTime(singleChatSettingsValue.getRecallTime());
		chatSettings.setPrivateTime(singleChatSettingsValue.getPrivateTime());
		chatSettings.setStatus(singleChatSettingsValue.getStatus().toString());
		chatSettings.setFeedbackFrom(singleChatSettingsValue.getFeedbackFrom());
		chatSettings.setFeedbackTo(singleChatSettingsValue.getFeedbackTo());
		chatSettings.setXmppDomain(singleChatSettingsValue.getXmppDomain());
		chatSettings.setSignalServerDomain(singleChatSettingsValue.getSignalServerDomain());
		chatSettings.setXmppHost(singleChatSettingsValue.getXmppHost());
		chatSettings.setGoogleTranslate(singleChatSettingsValue.getGoogleTranslate());
		chatSettings.setNotificationHelpUrl(singleChatSettingsValue.getNotificationHelpUrl());
		chatSettings.setXmppPort(singleChatSettingsValue.getXmppPort());
		chatSettings.setSdkUrl(singleChatSettingsValue.getSdkUrl());
		chatSettings.setPinExpireDays(singleChatSettingsValue.getPinExpireDays());
		chatSettings.setPinTimeOut(singleChatSettingsValue.getPinTimeOut());
		chatSettings.setFileSizeLimit(singleChatSettingsValue.getFileSizeLimit());
		chatSettings.setIsLiveStreamingEnabled(singleChatSettingsValue.getIsLiveStreamingEnabled());
		chatSettings.setLiveStreamingSignalServer(singleChatSettingsValue.getLiveStreamingSignalServer());
		chatSettings.setSipcallEnabled(singleChatSettingsValue.getSipcallEnabled());
		chatSettings.setSipServer(singleChatSettingsValue.getSipServer());
		chatSettings.setChatBackupType(singleChatSettingsValue.getChatBackupType());
		chatSettings.setChatBackupFrequency(singleChatSettingsValue.getChatBackupFrequency());
		chatSettings.setCallRoutingServer(singleChatSettingsValue.getCallRoutingServer());
		chatSettings.setXmppPortWeb(singleChatSettingsValue.getXmppPortWeb());
		chatSettings.setIv(singleChatSettingsValue.getIv());
		chatSettings.setIvProfile(singleChatSettingsValue.getIvProfile());

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

	private void updateChatSettings(ChatSettingDataDto request, ChatSettings chatSettingValue) {
		LocalDateTime currentTimestamp = LocalDateTime.now();
		chatSettingValue.setGroupChat(request.getGroupChat());
		chatSettingValue.setAttachment(request.getAttachment());
		chatSettingValue.setImageAttachment(request.getImageAttachment());
		chatSettingValue.setVideoAttachment(request.getVideoAttachment());
		chatSettingValue.setAudioAttachment(request.getAudioAttachment());
		chatSettingValue.setDocumentAttachment(request.getDocumentAttachment());
		chatSettingValue.setContactAttachment(request.getContactAttachment());
		chatSettingValue.setLocationAttachment(request.getLocationAttachment());
		chatSettingValue.setOne2oneCall(request.getOne2oneCall());
		chatSettingValue.setGroupCall(request.getGroupCall());
		chatSettingValue.setRecentchatSearch(request.getRecentchatSearch());
		chatSettingValue.setUpdatedAt(currentTimestamp);
		chatSettingsRepo.save(chatSettingValue);
	}

}
