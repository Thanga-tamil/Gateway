package com.mirrorfly.gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mirrorfly.gateway.datasourcerouter.AWSSesClientManager;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;
import com.mirrorfly.gateway.service.DataStore;

@Component
@Order(0)
public class StartupDataLoader implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private ConnectionConfigRepo connectionConfigRepo;

	@Autowired
	private AWSSesClientManager awsSesClientManager;

	@Autowired
	private DataStore dataStore;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		AbstractRoutingDataSourceImpl.setDatabaseName("default");
		List<ConnectionConfig> connectionConfig = connectionConfigRepo.findAll();
		/*
		 * Set All Connection-Config Data In-Memory
		 */
		dataStore.setConnectionConfig(connectionConfig);
		/*
		 * Create AWS SES Clients and Store In-Memory
		 */
		awsSesClientManager.initializeSesClients(connectionConfig);
	}

}
