package com.mirrorfly.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

@Configuration
public class GatewayConfiguration {

	@Autowired
	private ApplicationProperties applicationProperties;

	// AWS SES Bean Configuration
	AWSStaticCredentialsProvider awsCredentials() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(applicationProperties.getAccessKey(),
				applicationProperties.getSecretKey());
		return new AWSStaticCredentialsProvider(credentials);
	}

	@Bean
	AmazonSimpleEmailService getAmazonSimpleEmailService() {
		return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(awsCredentials())
				.withRegion("ap-south-1").build();
	}

}
