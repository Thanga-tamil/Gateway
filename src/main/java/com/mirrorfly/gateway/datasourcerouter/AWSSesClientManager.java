package com.mirrorfly.gateway.datasourcerouter;

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
import com.mirrorfly.gateway.service.DataStore;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AWSSesClientManager {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private DataStore dataStore;

	private final Map<String, AmazonSimpleEmailService> sesClients = new ConcurrentHashMap<>();

	/**
	 * Initializes the SES clients using the provided list of connection
	 * configurations.
	 *
	 * @param connectionConfigs The list of connection configurations.
	 */
	public void initializeSesClients(List<ConnectionConfig> connectionConfigs) {
		log.info("Initializing AWS SES clients at application startup...");

		for (ConnectionConfig config : connectionConfigs) {
			try {
				if (config.getEmailConfig() != null) {
					AmazonSimpleEmailService sesClient = createSESClient(config.getEmailConfig());
					sesClients.put(config.getApiDomain(), sesClient);
					log.info("Loaded SES client for domain: {}", config.getApiDomain());
				} else {
					log.warn("Skipped domain {}: Missing email configuration.", config.getApiDomain());
				}
			} catch (Exception e) {
				log.error("Failed to initialize SES client for domain: {}. Error: {}", config.getApiDomain(),
						e.getMessage());
			}
		}
		log.info("AWS SES client initialization completed. Total clients: {}", sesClients.size());
	}

	/**
	 * Creates a new SES client from the given email configuration.
	 *
	 * @param emailConfig The email configuration.
	 * @return The AWS SES client.
	 */
	private AmazonSimpleEmailService createSESClient(EmailConfig emailConfig) {
		log.debug("Creating SES client for region: {}", emailConfig.getRegion());
		AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(emailConfig.getAccessKeyId(), emailConfig.getSecretAccessKey()));
		return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(credentialsProvider)
				.withRegion(emailConfig.getRegion()).build();
	}

	/**
	 * Retrieves or creates the AWS SES client for a specific domain.
	 *
	 * @param domain The domain or user identifier for the client.
	 * @return The AWS SES client for the domain.
	 * @throws IllegalArgumentException if no client can be found or created.
	 */
	public AmazonSimpleEmailService getSesClient(String domain) {
		// Check if the SES client already exists in the in-memory map
		AmazonSimpleEmailService sesClient = sesClients.get(domain);

		if (sesClient != null) {
			log.debug("Found cached SES client for domain: {}", domain);
			return sesClient;
		}
		// Use the default database configuration to fetch emailConfig
		AbstractRoutingDataSourceImpl.setDatabaseName("default");
		try {
			// Fetch the email configuration for the domain
			EmailConfig emailConfig = entityManagerFactory.createEntityManager()
					.createQuery("SELECT c.emailConfig FROM ConnectionConfig c WHERE c.apiDomain = :apiDomain",
							EmailConfig.class)
					.setParameter("apiDomain", domain).getSingleResult();

			if (emailConfig == null) {
				log.warn("No email configuration found for domain: {}", domain);
				throw new IllegalArgumentException("No email configuration found for domain: " + domain);
			}
			// Create a new SES client using the emailConfig
			sesClient = createSESClient(emailConfig);

			// Cache the new SES client and update the in-memory store
			sesClients.put(domain, sesClient);
			dataStore.getConnectionConfigByDomain(domain).setEmailConfig(emailConfig);

			log.info("Created and cached new AWS SES client for domain: {}", domain);
			return sesClient;
		} catch (NoResultException e) {
			log.warn("No email configuration found in database for domain: {}", domain);
			throw new IllegalArgumentException("No email configuration found for domain: " + domain, e);

		} catch (Exception e) {
			log.error("Failed to create AWS SES client for domain: {}. Error: {}", domain, e.getMessage());
			throw new IllegalStateException("Failed to create AWS SES client for domain: " + domain, e);
		}
	}

	/**
	 * Cleans up resources during application shutdown.
	 */
	@PreDestroy
	public void cleanupResources() {
		log.info("Cleaning up AWS SES clients...");
		sesClients.clear();
		log.info("AWS SES clients cleanup complete.");
	}

}
