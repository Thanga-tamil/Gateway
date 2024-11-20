package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.mirrorfly.gateway.model.EmailConfig;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AWSEmailService {

	@Autowired
	private TemplateEngine templateEngine;

	@Async
	public void sendOtpEmail(EmailConfig emailConfig, String recipientEmail, String otp) throws MessagingException {
		log.info("Preparing to send OTP email to: {}", recipientEmail);

		Context context = new Context();
		context.setVariable("otpCode", otp); // Pass the OTP to the template

		try {
			// Generate HTML content from template
			String htmlContent = templateEngine.process("otp_email_template", context);
			log.debug("Generated OTP email content for: {}", recipientEmail);

			// Sending email using AWS SES
			if (sendAWSSES(emailConfig, recipientEmail, GatewayConstants.EMAIL_OTP_SUBJECT, htmlContent))
				log.info("OTP Email successfully sent to: {}", recipientEmail);
			else
				log.error("Failed to send OTP email to: {}", recipientEmail);
		} catch (Exception e) {
			log.error("Error generating OTP email content for: {}. Exception: {}", recipientEmail, e.getMessage());
			throw new MessagingException("Error occurred while preparing OTP email for " + recipientEmail, e);
		}
	}

	// AWS SES email sending method
	private boolean sendAWSSES(EmailConfig emailConfig, String recipient, String subject, String body) {
		log.info("Attempting to send email to: {}", recipient);
		try {
			AmazonSimpleEmailService amazonSimpleEmailService = createSESClient(emailConfig.getAccessKeyId(),
					emailConfig.getSecretAccessKey(), emailConfig.getRegion());
			log.debug("SES client created successfully with region: {}", emailConfig.getRegion());

			Destination destination = new Destination().withToAddresses(recipient);

			SendEmailRequest sendEmailRequest = new SendEmailRequest().withDestination(destination)
					.withMessage(new com.amazonaws.services.simpleemail.model.Message()
							.withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(body)))
							.withSubject(new Content().withCharset("UTF-8").withData(subject)))
					.withSource(emailConfig.getDefaultFromAddress());

			amazonSimpleEmailService.sendEmail(sendEmailRequest);
			log.info("Email sent successfully to: {}", recipient);
			return true;
		} catch (Exception e) {
			log.error("Error while sending email to: {}. Exception: {}", recipient, e.getMessage());
			return false;
		}
	}

	/**
	 * Creates an AWS SES client using the given access key, secret key, and region.
	 *
	 * @param accessKey the AWS access key
	 * @param secretKey the AWS secret key
	 * @param region    the AWS region
	 * @return AmazonSimpleEmailService instance
	 */
	private AmazonSimpleEmailService createSESClient(String accessKey, String secretKey, String region) {
		AWSStaticCredentialsProvider awsCredentials = new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(accessKey, secretKey));
		return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(awsCredentials).withRegion(region)
				.build();
	}

}
