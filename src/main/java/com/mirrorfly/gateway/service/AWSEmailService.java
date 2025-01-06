package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.mirrorfly.gateway.datasourcerouter.AWSSesClientManager;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.EmailConfig;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AWSEmailService {

	@Autowired
	private DataStore dataStore;

	@Autowired
	private AWSSesClientManager awsSesClientManager;

	@Autowired
	@Qualifier("customTemplateEngine")
	private TemplateEngine templateEngine;

	/**
	 * Sends an OTP email to the recipient.
	 *
	 * @param domain      The domain for the SES client.
	 * @param emailConfig Email configuration for the domain.
	 * @param recipient   Recipient email address.
	 * @param otp         The OTP to send.
	 */
	@Async
	public void sendOtpEmail(String domain, EmailConfig emailConfig, String recipient, String otp) {
		log.info("Preparing to send OTP email to: {}", recipient);

		try {
			// Send email
			sendAWSSES(domain, emailConfig, recipient, GatewayConstants.EMAIL_OTP_SUBJECT, otp);
			log.info("Successfully sent OTP email to: {}", recipient);
		} catch (Exception e) {
			log.error("Failed to send OTP email to {}. Error: {}", recipient, e.getMessage());
			throw new IllegalArgumentException("Error sending email: " + e.getMessage());
		}
	}

	/**
	 * Sends an email using AWS SES.
	 *
	 * @param domain      The domain for the SES client.
	 * @param emailConfig Email configuration for the domain.
	 * @param recipient   Recipient email address.
	 * @param subject     Email subject.
	 * @param body        Email body (HTML).
	 */
	private void sendAWSSES(String domain, EmailConfig emailConfig, String recipient, String subject, String otp) {
		log.info("Sending email to: {}", recipient);

		try {
			AmazonSimpleEmailService sesClient = awsSesClientManager.getSesClient(domain);

			if (emailConfig == null)
				emailConfig = dataStore.getConnectionConfigByDomain(domain).getEmailConfig();
			if (emailConfig == null) {
				log.error("Email configuration not found for domain: {}", domain);
				throw new BadDataException("Email configuration not found for domain: " + domain);
			}
			Context context = new Context();
			context.setVariable("otpCode", otp);

			String emailContent = templateEngine.process(emailConfig.getEmailTemplate(), context);
			log.debug("Generated email content for recipient: {}", recipient);

			Destination destination = new Destination().withToAddresses(recipient);
			SendEmailRequest request = new SendEmailRequest().withDestination(destination)
					.withMessage(new com.amazonaws.services.simpleemail.model.Message()
							.withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(emailContent)))
							.withSubject(new Content().withCharset("UTF-8").withData(subject)))
					.withSource(emailConfig.getDefaultFromAddress());

			sesClient.sendEmail(request);
			log.info("Email successfully sent to: {}", recipient);
		} catch (Exception e) {
			log.error("Failed to send email to: {}. Error: {}", recipient, e.getMessage());
			throw new IllegalArgumentException("Failed to send email: " + e.getMessage());
		}
	}

}
