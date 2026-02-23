package com.mirrorfly.gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;
import com.mirrorfly.gateway.repo.UserRepo;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AdminService {

	private ConnectionConfigRepo connectionConfigRepo;

	private DataStore dataStore;

	private ApplicationContext applicationContext;

	private ApplicationProperties applicationProperties;

	private UserRepo userRepo;

	private WebClient webClient;

	public AdminService() {
	}
	
	public AdminService(ConnectionConfigRepo connectionConfigRepo, DataStore dataStore, ApplicationContext applicationContext, ApplicationProperties applicationProperties, UserRepo userRepo, WebClient.Builder webClient) {
		this.connectionConfigRepo = connectionConfigRepo;
		this.dataStore = dataStore;
		this.applicationContext = applicationContext;
		this.applicationProperties = applicationProperties;
		this.userRepo = userRepo;
		this.webClient = webClient.baseUrl(applicationProperties.getPreProdUrl()).build();
	}

	public Map<String, Object> reloadConnectionConfig() {
		
		log.info("Gateway service inmemory data reload requested....");
		Map<String, Object> response = new HashMap<>();
		try {
			AbstractRoutingDataSourceImpl.setDatabaseName("default");
			List<ConnectionConfig> connectionConfigs = connectionConfigRepo.findAll();
			dataStore.setConnectionConfig(connectionConfigs);

			AbstractRoutingDataSourceImpl inMemoryDataSources = new AbstractRoutingDataSourceImpl();
			AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
			autowireCapableBeanFactory.autowireBean(inMemoryDataSources);

			inMemoryDataSources.refresh(connectionConfigs);

			response.put("status", HttpStatus.SC_OK);
			response.put("message", "Gateway service inmemory data reloaded successfully");

			log.info("Gateway service inmemory data reload request completed successfully");
		} catch (Exception e) {
			log.error("Could not reload gateway service inmemory data");
			return getErrorMessage("gateway");
		}
		return response;
	}

	@SwitchDataSource(value = "[0]")
	public boolean isAdmin(String userdomain, String username) {

		if (!applicationProperties.getSandboxUrl().startsWith(userdomain)) {
			return false;
		}

		Users user = userRepo.findByUsername(username);
		boolean result = false;
		if (user != null && user.getIsAdmin() == 1) {
			result = true;
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public Mono<Map> reloadAll(String uri, String token) {
		return this.webClient.get().uri(uri).accept(MediaType.APPLICATION_JSON).header("Authorization", token)
				.exchangeToMono(response -> {
					if (response.statusCode().isError()) {
						return response.createException().flatMap(Mono::error);
					} else if (response.statusCode().is2xxSuccessful()) {
						return response.bodyToMono(Map.class);
					} else {
						return response.createException().flatMap(Mono::error);
					}
				});
	}

	public Map<String, Object> getErrorMessage(String service) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", HttpStatus.SC_EXPECTATION_FAILED);
		response.put("message", "Could not reload " + service + " service inmemory data");
		return response;
	}
}
