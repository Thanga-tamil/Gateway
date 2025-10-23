package com.mirrorfly.gateway.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.ConnectionConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommonService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DataStore dataStore;

	@Transactional
	public void enableEncryptionMode() {
		log.debug("Enabling aes-256-cbc encryption mode on MySQL server");
		entityManager.createNativeQuery("SET block_encryption_mode ='aes-256-cbc';").executeUpdate();
		log.info("Encryption mode set to aes-256-cbc");
	}

	public ConnectionConfig getConnectionConfigByDomain(String userdomain) {
		return Optional.ofNullable(dataStore.getConnectionConfigByDomain(userdomain)).orElseThrow(() -> {
			log.warn("No connection configuration found for domain: {}", userdomain);
			return new IllegalArgumentException("No connection configuration found for domain: " + userdomain);
		});
	}

}
