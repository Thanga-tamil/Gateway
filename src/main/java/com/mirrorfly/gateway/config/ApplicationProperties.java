package com.mirrorfly.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisPassword;

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

	public String getRedisPort() {
		return env.getProperty("spring.redis.port");
	}

}
