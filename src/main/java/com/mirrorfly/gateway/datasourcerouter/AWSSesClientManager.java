package com.mirrorfly.gateway.datasourcerouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.EmailConfig;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;
import com.mirrorfly.gateway.service.DataStore;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AWSSesClientManager {

	@Autowired
	private ConnectionConfigRepo connectionConfigRepo;

	@Autowired
	private DataStore dataStore;

	private final Map<String, AmazonSimpleEmailService> sesClients = new ConcurrentHashMap<>();

	/**
	 * Initializes the SES clients using the provided list of connection
	 * configurations.
	 *
	 * @param connectionConfigs The list of connection configurations.
	 */
	public void initializeSesClients() {
		log.info("Initializing AWS SES clients...");

		List<ConnectionConfig> connectionConfigs = new ArrayList<>(dataStore.getConnectionConfig().values());

		int successCount = 0;
		int failureCount = 0;

		for (ConnectionConfig config : connectionConfigs) {
			try {
				if (config.getEmailConfig() != null) {
					AmazonSimpleEmailService sesClient = createSESClient(config.getEmailConfig());
					sesClients.put(config.getApiDomain(), sesClient);
					log.info("Successfully created AWS SES client for domain: {}", config.getApiDomain());
					successCount++;
				} else {
					log.warn("Skipped AWS SES client creation: Email configuration is missing for domain: {}",
							config.getApiDomain());
				}
			} catch (Exception e) {
				failureCount++;
				log.warn("Failed to create AWS SES client for domain: {}. Error: {}", config.getApiDomain(),
						e.getMessage());
			}
		}
		log.info("AWS SES client initialization completed. Success: {}, Failed: {}, Total domains: {}", successCount,
				failureCount, connectionConfigs.size());
	}

	/**
	 * Creates a new AWS SES client from the given email configuration.
	 *
	 * @param emailConfig The email configuration.
	 * @return The AWS SES client.
	 */
	private AmazonSimpleEmailService createSESClient(EmailConfig emailConfig) {
		AWSStaticCredentialsProvider awsCredentials = new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(emailConfig.getAccessKeyId(), emailConfig.getSecretAccessKey()));
		return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(awsCredentials)
				.withRegion(emailConfig.getRegion()).build();
	}

	/**
	 * Retrieves the AWS SES client for a specific domain or user. If the client
	 * does not exist in memory, it will check if the Email-Config for the domain
	 * exists and create a new SES client.
	 *
	 * @param domain The domain or user identifier for the client.
	 * @return The AWS SES client for the domain.
	 * @throws IllegalArgumentException if no client is found or could not be
	 *                                  created for the domain.
	 */
	public AmazonSimpleEmailService getSesClient(String domain) {
		// First, check if the SES client is already in the in-memory map
		AmazonSimpleEmailService sesClient = sesClients.get(domain);

		if (sesClient != null) {
			return sesClient; // Return the existing SES client if it exists
		}
		AbstractRoutingDataSourceImpl.setDatabaseName("default");
		// If not found, try to get the email configuration for the domain
		EmailConfig emailConfig = connectionConfigRepo.getEmailConfigByApiDomain(domain);
		if (emailConfig != null) {
			try {
				// Create a new SES client using the email-config
				sesClient = createSESClient(emailConfig);

				// Add the newly created client to the in-memory map
				sesClients.put(domain, sesClient);
				log.info("Created and cached new AWS SES client for domain: {}", domain);
				return sesClient;
			} catch (Exception e) {
				log.error("Failed to create AWS SES client for domain: {}. Error: {}", domain, e.getMessage());
				throw new IllegalArgumentException("Failed to create AWS SES client for domain: " + domain);
			}
		} else {
			log.warn("No email configuration found for domain: {}", domain);
			throw new IllegalArgumentException("No email configuration found for domain: " + domain);
		}
	}

}
