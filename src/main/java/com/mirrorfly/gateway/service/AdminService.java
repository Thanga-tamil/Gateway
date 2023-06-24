package com.mirrorfly.gateway.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.response.StringResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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
	
    private WebClient webClient;
    
    @Autowired
    private UserRepo userRepo;
    
	@Autowired
	private RestTemplate restTemplate;
    
    public AdminService(WebClient.Builder webClientBuilder, ApplicationProperties applicationProperties) {
    	this.webClient = webClientBuilder.baseUrl(applicationProperties.getSandboxUrl()).build();
    }

	public StringResponse reloadConnectionConfig() {
		log.info("Gateway service inmemory data reload requested....");
		StringResponse response = new StringResponse();
		try {
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
	
    public Mono<StringResponse> reloadAll(String uri, String token) {
    	return this.webClient.get()
    		     .uri(uri)
    		     .accept(MediaType.APPLICATION_JSON)
    		     .header("Authorization", token)
    		     .exchangeToMono(response -> {
    	             if (response.statusCode().isError()) {
    	                 return response.createException().flatMap(Mono::error);
    	             }else if (response.statusCode().is2xxSuccessful()) {
    		             return response.bodyToMono(StringResponse.class);
    		         }else {
    		             return response.createException().flatMap(Mono::error);
    		         }
    		     });
    }
    
	public StringResponse getErrorMessage(String service) {
		StringResponse response = new StringResponse();
		response.setStatus(HttpStatus.SC_EXPECTATION_FAILED);
		response.setMessage("Could not reload "+service+" service inmemory data.");
		return response;
	}
	
	public void reload(String uri, String token) {
		String url = applicationProperties.getSandboxUrl() + uri;
		try {
			
			List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();        
			MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
			converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));        
			messageConverters.add(converter);
			restTemplate.setMessageConverters(messageConverters); 
			
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			headers.set("Authorization", token);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
			HttpEntity<?> entity = new HttpEntity<>(headers);
			restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Response.class);
		} catch (HttpStatusCodeException e) {
			handlingExceptionOfApiCall(e);
		}
	}
	
	private void handlingExceptionOfApiCall(HttpStatusCodeException e) {
		String responseBody = e.getResponseBodyAsString();
		ObjectMapper mapper = new ObjectMapper();
		try {
			Response<?> r = mapper.readValue(responseBody, Response.class);
			throw new BadDataException(r.getMessage());
		} catch (JsonProcessingException ex) {
			throw new BadDataException("Error in inmemory refresh. Please try again later");
		}
	}

}
