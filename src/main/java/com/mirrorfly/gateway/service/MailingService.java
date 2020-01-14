package com.mirrorfly.gateway.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import com.mirrorfly.gateway.model.MailProperties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailingService {

	private final MailProperties mailProperties;
	private final Configuration templates;

	@Autowired
	MailingService(MailProperties mailProperties, Configuration templates) {
		this.mailProperties = mailProperties;
		this.templates = templates;
	}

	public boolean sendForgotPasswordMail(String toEmail) {
		String subject = "Forgot Password";
		String body = "";
		try {
			Template t = templates.getTemplate("forgot-password.ftl");
			Map<String, String> map = new HashMap<>();
			map.put("RESET_PASSWORD", "www.google.com");

			body = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
		} catch (Exception e) {
			log.error(this.getClass().getName() + e.getMessage(), e);
		}
		return sendMail(toEmail, subject, body);
	}

	private boolean sendMail(String toEmail, String subject, String body) {
		try {
			Properties props = System.getProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.port", mailProperties.getSmtp().getPort());
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");

			Session session = Session.getDefaultInstance(props);
			session.setDebug(true);

			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailProperties.getFrom(), mailProperties.getFromName()));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			msg.setSubject(subject);
			msg.setContent(body, "text/html");

			Transport transport = session.getTransport();
			transport.connect(mailProperties.getSmtp().getHost(), mailProperties.getSmtp().getUsername(),
					mailProperties.getSmtp().getPassword());
			transport.sendMessage(msg, msg.getAllRecipients());
			return true;
		} catch (Exception ex) {
			log.error(this.getClass().getName() + ex.getMessage(), ex);
		}

		return false;
	}

}
