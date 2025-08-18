package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.EmailConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SMTPMailService {

	@Autowired
	@Qualifier("customTemplateEngine")
	private TemplateEngine templateEngine;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ApplicationProperties applicationProperties;

	public void sendSMTPMails(EmailConfig emailConfig, String email, String otp) {
		log.info("Sending email to: {}", email);
		try {
			String emailContent = generateEmailContent(emailConfig.getEmailTemplate(), otp);
			log.debug("Generated email content for recipient: {}", email);
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(email);
			message.setSubject(emailConfig.getSubject());
			message.setText(emailContent);
			message.setFrom(applicationProperties.getMailSource()); // Change to a verified sender from SendGrid
			mailSender.send(message);
		} catch (Exception e) {
			log.error("Unexpected error: {}", e.getMessage());
			throw new IllegalArgumentException("Failed to send email: " + e.getMessage());
		}
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