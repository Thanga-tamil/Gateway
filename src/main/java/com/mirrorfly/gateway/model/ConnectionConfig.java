package com.mirrorfly.gateway.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "connection_config")
public class ConnectionConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "mysql", columnDefinition = "JSON")
	private String mysql;

	@Column(name = "api_domain")
	private String apiDomain;

	@Column(name = "xmpp_domain")
	private String xmppDomain;

	@Column(name = "signal_domain")
	private String signalDomain;

	@Column(name = "xmpp_config", columnDefinition = "JSON")
	private String xmppConfig;

	@Column(name = "license_key")
	private String licenseKey;

	@Column(name = "storage")
	private String storage;

	@Column(name = "aws_details", columnDefinition = "JSON")
	private String awsDetails;

	@Column(name = "oci_details", columnDefinition = "JSON")
	private String ociDetails;

	@Column(name = "android_push_details", columnDefinition = "json")
	private String androidPushDetails;

	@Column(name = "ios_push_details", columnDefinition = "json")
	private String iosPushDetails;

	@Column(name = "user_max_sessions")
	private int userMaxSessions;

}