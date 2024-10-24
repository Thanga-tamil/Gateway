package com.mirrorfly.gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommonService {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void enableEncryptionMode() {
		log.debug("Enabling aes-256-cbc encryption mode on MySQL server");
		entityManager.createNativeQuery("SET block_encryption_mode ='aes-256-cbc';").executeUpdate();
		log.info("Encryption mode set to aes-256-cbc");
	}

}
