package com.mirrorfly.gateway.dto;

import java.util.List;

import lombok.Data;

@Data
public class ConfigDto {

	private String domain;

	private Integer videoLimit;

	private Integer audioLimit;

	private Integer recallTime;

	private Integer privateTime;

	private String xmppDomain;

	private String xmppHost;

	private Integer xmppPort;

	private String adminUser;

	private String googleTranslate;

	private String signalServerDomain;

	private String notificationHelpUrl;

	private String sdkUrl;

	private Integer pinExpireDays;

	private Integer pinTimeOut;

	private Integer fileSizeLimit;

	private List<String> stuns;

	private List<TurnDto> turns;

	private String liveStreamingSignalServer;

	private Boolean isLiveStreamingEnabled;

	private Boolean sipcallEnabled;

	private String sipServer;

	private String callRoutingServer;

	private String chatBackupType;

	private String chatBackupFrequency;

	private String xmppPortWeb;

	private String iv;

	private String ivProfile;

	private Boolean groupChat;

	private Boolean attachment;

	private Boolean imageAttachment;

	private Boolean videoAttachment;

	private Boolean audioAttachment;

	private Boolean documentAttachment;

	private Boolean contactAttachment;

	private Boolean locationAttachment;

	private Boolean one2oneCall;

	private Boolean groupCall;

	private Boolean recentchatSearch;

}