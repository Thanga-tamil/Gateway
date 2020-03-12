package com.mirrorfly.gateway.queue;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mirrorfly.gateway.config.ApplicationProperties;
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
			log.error("Error in Sending to RabbitMQ Mail Exchange", a);
			return false;
		} catch (Exception a) {
			log.error("Error in Sending to Mail Queue", a);
			return false;

		}
		return true;
	}

}
