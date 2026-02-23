package com.mirrorfly.gateway.queue;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.QueuePayloadDto;
import com.mirrorfly.gateway.util.EnumQueue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueueService {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ApplicationProperties applicationProperties;

	public boolean sendMailPayload(MailPayload mailPayload) {
		try {
			rabbitTemplate.convertAndSend(applicationProperties.getMailQueue(), EnumQueue.EMAIL_QUEUE.getQueue(),
					mailPayload);
		} catch (AmqpException a) {
			log.error("Error in Sending to RabbitMQ Mail Exchange ==> " + a.getMessage());
			return false;
		} catch (Exception a) {
			log.error("Error in Sending to Mail Queue ==> " + a.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean sendLicenseDataPayload(LicenseDataPayload licenseDataPayload) {
		try {
			rabbitTemplate.convertAndSend(applicationProperties.getLicenseDataQueue(),
					EnumQueue.LICENSE_QUEUE.getQueue(), licenseDataPayload);
		} catch (AmqpException a) {
			log.error("Error in Sending to RabbitMQ Mail Exchange ==> " + a.getMessage());
			return false;
		} catch (Exception a) {
			log.error("Error in Sending to Mail Queue ==> " + a.getMessage());
			return false;
		}
		return true;
	}

	public boolean sendUserSessionLogoutActivity(QueuePayloadDto payload) {
		try {
			log.info("Multi-Sessions Logout RabbitMQ payload ::: " + payload);
			rabbitTemplate.convertAndSend(EnumQueue.API_DATA_QUEUE.getQueue(), payload);
			return true;
		} catch (Exception e) {
			log.error("Failed to send logout payload to RabbitMQ ==> " + e.getMessage());
			return false;
		}
	}

}
