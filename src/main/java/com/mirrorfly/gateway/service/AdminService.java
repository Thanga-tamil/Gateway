package com.mirrorfly.gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.StringResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminService {

	@Autowired
	private ConnectionConfigRepo connectionConfigRepo;

	@Autowired
	private DataStore dataStore;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
    @Autowired
    private UserRepo userRepo;
    
	@Autowired
	private RestTemplate restTemplate;
    
    public StringResponse reloadConnectionConfig() {
		log.info("Gateway service inmemory data reload requested....");
		StringResponse response = new StringResponse();
		try {
			AbstractRoutingDataSourceImpl.setDatabaseName("default");
			List<ConnectionConfig> connectionConfigs = connectionConfigRepo.findAll();
			dataStore.setConnectionConfig(connectionConfigs);

			AbstractRoutingDataSourceImpl inMemoryDataSources = new AbstractRoutingDataSourceImpl();
			AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
			autowireCapableBeanFactory.autowireBean(inMemoryDataSources);

			inMemoryDataSources.refresh(connectionConfigs);

			response.setStatus(HttpStatus.SC_OK);
			response.setMessage("Gateway service inmemory data reloaded successfully.");
			log.info("Gateway service inmemory data reload request completed successfully");
		} catch (Exception e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.setMessage("Could not reload gateway service inmemory data");
			log.error("Could not reload gateway service inmemory data");
		}
		return response;
	}
	
	@SwitchDataSource(value="[0]")
	public boolean isAdmin(String userdomain, String username) {
		
		if(!applicationProperties.getSandboxUrl().startsWith(userdomain)) {
			return false;
		}
		
		Users user = userRepo.findByUsername(username);
		boolean result = false;
		if(user != null && user.getIsAdmin() == 1) {
			result = true;
		}
		return result;
	}
	
    public String reload(String uri, String token, String service) {
		try {
			String url = applicationProperties.getSandboxUrl() + uri;
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
			headers.set("Authorization", token);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
			HttpEntity<?> entity = new HttpEntity<>(headers);
			ResponseEntity<String> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
			return exchange.getBody();
		}catch(Exception e) {
			log.error(e.getMessage());			
			return getErrorMessage(service);
		}
	}
	
	public String getErrorMessage(String service){
		Map<String, Object> response = new HashMap<>();
		response.put("status", HttpStatus.SC_EXPECTATION_FAILED);
		response.put("message", "Could not reload "+service+" service inmemory data");
		try {
			return convertMapToJson(response);
		}catch(Exception e) {
			return "";
		}
	}
	
	public String convertMapToJson(Map<String, Object> messageContent) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(messageContent);
	}

}
