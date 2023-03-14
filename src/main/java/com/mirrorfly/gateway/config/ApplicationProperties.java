package com.mirrorfly.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ApplicationProperties {

	@Autowired
	private Environment env;

	public Long getTokenExpiryTime() {
		return Long.parseLong(env.getProperty("token_Expiry_Time"));
	}

	public String getAWSSecretAccessKey() {
		return env.getProperty("aws_secret_access_key");
	}

	public String getAWSAccessKey() {
		return env.getProperty("aws_access_key_id");
	}

	public String getAWSS3Region() {
		return env.getProperty("aws_s3_region");
	}

	public String getAWSS3BucketName() {
		return env.getProperty("aws_s3_bucketname");
	}

	public String getFcmURL() {
		return env.getProperty("fcm_url");
	}

	public String getRedisHost() {
		return env.getProperty("spring.redis.host");
	}

	public String getRedisPassword() {
		return env.getProperty("spring.redis.password");
	}

	public String getRedisPort() {
		return env.getProperty("spring.redis.port");
	}

	public String getApiBaseUrl() {
		return env.getProperty("api_base_url");
	}

	public String getMailQueue() {
		return env.getProperty("api.mail.exchange");
	}

	public String getResetPasswordRedirectUrl() {
		return env.getProperty("reset_password_redirect_url");
	}

	public String getWebportalApiBaseUrl() {
		return env.getProperty("webportal.api_base_url");
	}

	public String getLicenseKey() {
		return env.getProperty("licenseKey");
	}

	public String getLicenseDataQueue() {
		return env.getProperty("queue.licensedata");
	}

	public String getXmppDomain() {
		return env.getProperty("xmpp_domain");
	}

	public String getWhiteListIps() {
		return env.getProperty("white-list-ips");
	}
	
	public int getMaximumSessions() {
		return env.getProperty("user.maxSessions") != null ? Integer.parseInt(env.getProperty("user.maxSessions")) : 2;
	}
	
	public int getMetaDataLimit() {
		return env.getProperty("metadata.limit", Integer.class, 3);
	}
}

