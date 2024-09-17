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
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;

import com.mirrorfly.gateway.config.ApplicationProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AWSEmailService {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private AmazonSimpleEmailService amazonSimpleEmailService;

	@Async
	public void sendOtpEmail(String recipientEmail, String otp) throws MessagingException {
		Context context = new Context();
		context.setVariable("otpCode", otp); // Pass the OTP to the template
		// Generate HTML content from template
		String htmlContent = templateEngine.process("otp_email_template", context);
		// Sending email using AWS SES
		if (sendAWSSES(recipientEmail, "Your SafeTalk OTP", htmlContent))
			log.info("OTP Email successfully sent to: {}", recipientEmail);
		else
			log.error("Failed to send OTP email to: {}", recipientEmail);
	}

	// AWS SES email sending method
	public boolean sendAWSSES(String recipient, String subject, String body) {
		try {
			Destination destination = new Destination().withToAddresses(recipient);

			SendEmailRequest sendEmailRequest = new SendEmailRequest().withDestination(destination)
					.withMessage(new com.amazonaws.services.simpleemail.model.Message()
							.withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(body)))
							.withSubject(new Content().withCharset("UTF-8").withData(subject)))
					.withSource(applicationProperties.getSafeTalkEmail());

			amazonSimpleEmailService.sendEmail(sendEmailRequest);
			return true;
		} catch (Exception e) {
			log.error("Error while sending email to {}: {}", recipient, e.getMessage());
			throw new MessagingException("Error occurred while sending email: " + recipient);
		}
	}

}
