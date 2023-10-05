package com.mirrorfly.gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataStore {

	@Autowired
	private ConnectionConfigRepo connectionConfigRepo;

	private Map<String, ConnectionConfig> connectionConfig = new ConcurrentHashMap<>();

	public void setConnectionConfig(List<ConnectionConfig> config) {
		connectionConfig = new ConcurrentHashMap<>();
		config.forEach(data -> connectionConfig.put(data.getApiDomain(), data));
		log.info("ConnectionConfig loaded into inmemory. Size is : " + connectionConfig.size() + ", Current data is "
				+ connectionConfig);
	}

	public void reloadConnectionConfig() {
		AbstractRoutingDataSourceImpl.setDatabaseName("default");
		connectionConfig = new ConcurrentHashMap<>();
		connectionConfigRepo.findAll().forEach(config -> connectionConfig.put(config.getApiDomain(), config));
		log.info("ConnectionConfig inmemory refreshed. Size is : " + connectionConfig.size() + ", Current data is "
				+ connectionConfig);
	}

	public Map<String, ConnectionConfig> getConnectionConfig() {
		return connectionConfig;
	}

	public ConnectionConfig getConnectionConfigByDomain(String domain) {
		ConnectionConfig config = connectionConfig.get(domain);
		if (config == null) {
			log.info("Could not find ConnectionConfig data for domain " + domain
					+ " in inmemory. Reloading connection_config table.");
			reloadConnectionConfig();
			config = connectionConfig.get(domain);
		}
		// setting back the active datasource name to whatever it was before calling
		// this method
		AbstractRoutingDataSourceImpl.setDatabaseName(domain);
		return config;
	}

	public Object getConnectionConfigDataByDomainAndKey(String domain, String key) {
		ConnectionConfig config = getConnectionConfigByDomain(domain);
		Map<String, Object> configAsMap = new HashMap<>();
		if (config != null) {
			ObjectMapper mapper = new ObjectMapper();
			configAsMap = mapper.convertValue(config, new TypeReference<Map<String, Object>>() {
			});
		}
		return configAsMap.get(key);
	}

}
