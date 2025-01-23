package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.EmailConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AWSEmailService {

	@Autowired
	@Qualifier("customTemplateEngine")
	private TemplateEngine templateEngine;

	/**
	 * Sends an email using AWS SES.
	 *
	 * @param domain      The domain for the SES client.
	 * @param emailConfig Email configuration for the domain.
	 * @param recipient   Recipient email address.
	 * @param subject     Email subject.
	 * @param body        Email body (HTML).
	 */
	@Async
	public void sendAWSSES(EmailConfig emailConfig, String recipient, String otp) {
		log.info("Sending email to: {}", recipient);

		try {
			// Create SES Client for Sending email
			AmazonSimpleEmailService sesClient = createSESClient(emailConfig);

			String emailContent = generateEmailContent(emailConfig.getEmailTemplate(), otp);
			log.debug("Generated email content for recipient: {}", recipient);

			Destination destination = new Destination().withToAddresses(recipient);
			SendEmailRequest request = new SendEmailRequest().withDestination(destination)
					.withMessage(new com.amazonaws.services.simpleemail.model.Message()
							.withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(emailContent)))
							.withSubject(new Content().withCharset("UTF-8").withData(emailConfig.getSubject())))
					.withSource(emailConfig.getDefaultFromAddress());

			sesClient.sendEmail(request);
			log.info("Email successfully sent to: {}", recipient);

		} catch (AmazonServiceException e) {
			log.error("AWS SES service error: {}", e.getErrorMessage());
			throw new IllegalArgumentException("AWS SES error: " + e.getErrorMessage());
		} catch (TemplateInputException e) {
			log.error("Error processing email template: {}", e.getMessage());
			throw new IllegalArgumentException("Email template processing error: " + e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error: {}", e.getMessage());
			throw new IllegalArgumentException("Failed to send email: " + e.getMessage());
		}
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

	private String generateEmailContent(String template, String otp) {
		if (template == null || template.isEmpty()) {
			throw new BadDataException("Email template is missing");
		}
		Context context = new Context();
		context.setVariable("otpCode", otp);
		return templateEngine.process(template, context);
	}

}
