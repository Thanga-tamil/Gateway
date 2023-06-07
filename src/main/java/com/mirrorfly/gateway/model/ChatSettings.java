package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.sql.Timestamp;
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
	
	@Column(name = "license_key")
	private String licenseKey;

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
	
	@Column(name = "license_expiry")
	private Timestamp license_expiry;

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

	@Column(name = "group_chat")
	private Integer groupChat = 1;

	@Column(name = "attachment")
	private Integer attachment = 1;

	@Column(name = "image_attachment")
	private Integer imageAttachment = 1;

	@Column(name = "video_attachment")
	private Integer videoAttachment = 1;

	@Column(name = "audio_attachment")
	private Integer audioAttachment = 1;

	@Column(name = "document_attachment")
	private Integer documentAttachment = 1;

	@Column(name = "contact_attachment")
	private Integer contactAttachment = 1;

	@Column(name = "location_attachment")
	private Integer locationAttachment = 1;

	@Column(name = "one2one_call")
	private Integer one2oneCall = 1;

	@Column(name = "group_call")
	private Integer groupCall = 1;
	
	@Column(name = "recentchat_search")
	private Integer recentchatSearch = 1;
	
	@Column(name = "star_message")
	private Integer starMessage = 1;
	
	@Column(name = "clear_chat")
	private Integer clearChat = 1;
	
	@Column(name = "delete_chat")
	private Integer deleteChat = 1;
	
	@Column(name = "translation")
	private Integer translation = 1;
	
	@Column(name = "block")
	private Integer block = 1;
	
	@Column(name = "report")
	private Integer report = 1;
	
	@Column(name = "delete_message")
	private Integer deleteMessage = 1;
	
	@Column(name = "view_all_medias")
	private Integer viewAllMedias = 1;

}
