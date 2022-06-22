package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "chat_settings")
@Data
public class ChatSettings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "domain")
	private String domain;

	@Column(name = "admin_user")
	private String adminUser;

	@Column(name = "videoLimit")
	private Integer videoLimit;

	@Column(name = "mail_login")
	private String mailLogin;

	@Column(name = "audio_limit")
	private Integer audioLimit;

	@Column(name = "recall_time")
	private Integer recallTime;

	@Column(name = "private_time")
	private Integer privateTime;

	@Column(name = "status")
	private String status;

	@Column(name = "feedback_from")
	private String feedbackFrom;

	@Column(name = "feedback_to")
	private String feedbackTo;

	@Column(name = "xmpp_server")
	private String xmppDomain;

	@Column(name = "xmpp_host")
	private String xmppHost;

	@Column(name = "signal_server")
	private String signalServerDomain;

	@Column(name = "xmpp_port")
	private Integer xmppPort;

	@Column(name = "google_translate")
	private String googleTranslate;

	@Column(name = "notification_help")
	private String notificationHelpUrl;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "sipcall_enabled")
	private Boolean sipcallEnabled;

	@Column(name = "sip_server")
	private String sipServer;

	@Column(name = "call_routing_server")
	private String callRoutingServer;

	@Column(name = "chat_backup_type")
	private String chatBackupType;

	@Column(name = "chat_backup_frequency")
	private String chatBackupFrequency;

	@Column(name = "file_size_limit")
	private Integer fileSizeLimit;

	@Column(name = "sdk_url")
	private String sdkUrl;

	@Column(name = "pin_expire_days")
	private Integer pinExpireDays;

	@Column(name = "pin_timeout")
	private Integer pinTimeOut;

	@Column(name = "livestreaming_signalserver")
	private String liveStreamingSignalServer;

	@Column(name = "livestreaming_enabled")
	private Integer isLiveStreamingEnabled;

	@Column(name = "xmpp_port_web")
	private String xmppPortWeb;

	@Column(name = "iv")
	private String iv;

	@Column(name = "iv_profile")
	private String ivProfile;

}
