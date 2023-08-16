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

	@Column(name = "customer_domain")
	private String customerDomain;

	@Column(name = "connection_string")
	private String connectionString;

	@Column(name = "db_username")
	private String dbUsername;

	@Column(name = "db_password")
	private String dbPassword;

	@Column(name = "minimum_connections")
	private int minimumConnections;

	@Column(name = "maximum_connections")
	private int maximumConnections;

	@Column(name = "license_key")
	private String licenseKey;

	@Column(name = "storage")
	private String storage;

	@Column(name = "xmpp_domain")
	private String xmppDomain;

	@Column(columnDefinition = "JSON", name = "aws_details")
	private String awsDetails;

	@Column(columnDefinition = "JSON", name = "oci_details")
	private String ociDetails;

	@Column(name = "max_sessions")
	private int maxSessions;

	@Column(columnDefinition = "JSON", name = "xmpp_config")
	private String xmppConfig;

	@Column(name = "apns_certificate_url")
	private String apnsCertificateUrl;

	@Column(name = "apns_certificate_password")
	private String apnsCertificatePassword;

	@Column(name = "voip_certificate_url")
	private String voipCertificateUrl;

	@Column(name = "voip_certificate_password")
	private String voipCertificatePassword;

	@Column(name = "ios_bundle_id")
	private String iosBundleId;

	@Column(name = "firebase_key")
	private String firebaseKey;

}